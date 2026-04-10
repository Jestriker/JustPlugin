import { NextRequest, NextResponse } from "next/server";

// Set these in your .env.local:
// TURNSTILE_SECRET_KEY=your_secret_key
// CONTACT_DISCORD_WEBHOOK=https://discord.com/api/webhooks/...

// --- Rate limiter (in-memory, per IP) ---
const rateLimit = new Map<string, { count: number; resetAt: number }>();
const RATE_LIMIT_MAX = 3;       // max submissions
const RATE_LIMIT_WINDOW = 600000; // per 10 minutes

function checkRateLimit(ip: string): { allowed: boolean; retryAfter?: number } {
  const now = Date.now();
  const entry = rateLimit.get(ip);

  if (!entry || now > entry.resetAt) {
    rateLimit.set(ip, { count: 1, resetAt: now + RATE_LIMIT_WINDOW });
    return { allowed: true };
  }

  if (entry.count >= RATE_LIMIT_MAX) {
    const retryAfter = Math.ceil((entry.resetAt - now) / 1000);
    return { allowed: false, retryAfter };
  }

  entry.count++;
  return { allowed: true };
}

// Clean up stale entries every 5 minutes
setInterval(() => {
  const now = Date.now();
  for (const [ip, entry] of rateLimit) {
    if (now > entry.resetAt) rateLimit.delete(ip);
  }
}, 300000);

function isDiscordId(value: string): boolean {
  return /^\d{15,21}$/.test(value);
}

function getDeviceType(ua: string): string {
  if (/mobile|android|iphone|ipod/i.test(ua)) return "Mobile";
  if (/tablet|ipad/i.test(ua)) return "Tablet";
  return "Desktop";
}

function getBrowser(ua: string): string {
  if (/edg\//i.test(ua)) return "Edge";
  if (/opr\//i.test(ua) || /opera/i.test(ua)) return "Opera";
  if (/firefox\//i.test(ua)) return "Firefox";
  if (/safari\//i.test(ua) && !/chrome\//i.test(ua)) return "Safari";
  if (/chrome\//i.test(ua)) return "Chrome";
  return "Unknown";
}

function getOS(ua: string): string {
  if (/windows nt 10/i.test(ua)) return "Windows 10/11";
  if (/windows/i.test(ua)) return "Windows";
  if (/mac os x/i.test(ua)) return "macOS";
  if (/android/i.test(ua)) return "Android";
  if (/iphone|ipad/i.test(ua)) return "iOS";
  if (/linux/i.test(ua)) return "Linux";
  return "Unknown";
}

export async function POST(req: NextRequest) {
  // Get IP
  const ip = req.headers.get("x-forwarded-for")?.split(",")[0]?.trim()
    || req.headers.get("x-real-ip")
    || "Unknown";

  // Rate limit check
  const limit = checkRateLimit(ip);
  if (!limit.allowed) {
    return NextResponse.json(
      { error: `You're submitting too fast. Please try again in ${limit.retryAfter} seconds.` },
      { status: 429 }
    );
  }

  try {
    const body = await req.json();
    const {
      name, email, discord, category, subject, description, turnstileToken,
      userAgent, language, screenRes, timezone,
    } = body;

    // Validate required fields
    if (!name || !email || !category || !subject || !description) {
      return NextResponse.json({ error: "All required fields must be filled." }, { status: 400 });
    }

    // Validate email format
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email)) {
      return NextResponse.json({ error: "Please enter a valid email address." }, { status: 400 });
    }

    // Validate Turnstile token
    const turnstileSecret = process.env.TURNSTILE_SECRET_KEY;
    if (turnstileSecret) {
      if (!turnstileToken) {
        return NextResponse.json({ error: "Please complete the captcha verification." }, { status: 400 });
      }
      try {
        const verifyRes = await fetch("https://challenges.cloudflare.com/turnstile/v0/siteverify", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: new URLSearchParams({
            secret: turnstileSecret,
            response: turnstileToken,
            remoteip: ip,
          }),
          signal: AbortSignal.timeout(5000),
        });
        const verifyData = await verifyRes.json();
        if (!verifyData.success) {
          return NextResponse.json({ error: "Captcha verification failed. Please refresh and try again." }, { status: 403 });
        }
      } catch {
        return NextResponse.json({ error: "Could not verify captcha. Please try again." }, { status: 503 });
      }
    }

    // Geo lookup via IP (free, no key needed)
    let country = "Unknown";
    let city = "Unknown";
    try {
      const geoRes = await fetch(`https://ipapi.co/${ip}/json/`, { signal: AbortSignal.timeout(3000) });
      if (geoRes.ok) {
        const geo = await geoRes.json();
        if (geo.country_name) country = geo.country_name;
        if (geo.city) city = geo.city;
      }
    } catch {
      // Geo lookup failed — not critical
    }

    // Parse device info
    const ua = userAgent || req.headers.get("user-agent") || "Unknown";
    const browser = getBrowser(ua);
    const os = getOS(ua);
    const device = getDeviceType(ua);

    // Discord field — detect ID vs username
    let discordDisplay = "";
    let discordMention = "";
    if (discord) {
      if (isDiscordId(discord)) {
        discordDisplay = `<@${discord}> (\`${discord}\`)`;
        discordMention = `<@${discord}>`;
      } else {
        discordDisplay = `@${discord.replace(/^@/, "")}`;
        discordMention = `@${discord.replace(/^@/, "")}`;
      }
    }

    // Timestamps
    const now = new Date();
    const unixTimestamp = Math.floor(now.getTime() / 1000);

    // Send to Discord webhook
    const webhookUrl = process.env.CONTACT_DISCORD_WEBHOOK;
    if (webhookUrl) {
      const embed = {
        title: `[${category}] ${subject.substring(0, 200)}`,
        color: 0xf97316,
        fields: [
          { name: "Name", value: name.substring(0, 256), inline: true },
          { name: "Email", value: email.substring(0, 256), inline: true },
          ...(discord ? [{ name: "Discord", value: discordDisplay, inline: true }] : []),
          { name: "\u200b", value: "\u200b", inline: false },
          { name: "Description", value: description.substring(0, 1024), inline: false },
          { name: "\u200b", value: "\u200b", inline: false },
          { name: "Submitted", value: `<t:${unixTimestamp}:F> (<t:${unixTimestamp}:R>)`, inline: true },
          { name: "Timezone", value: timezone || "Unknown", inline: true },
          { name: "\u200b", value: "\u200b", inline: false },
          {
            name: "Sensitive Info (spoilered)",
            value: [
              `**IP:** ||${ip}||`,
              `**Country:** ||${country}${city !== "Unknown" ? `, ${city}` : ""}||`,
              `**Browser:** ||${browser}||`,
              `**OS:** ||${os}||`,
              `**Device:** ||${device}||`,
              ...(screenRes ? [`**Screen:** ||${screenRes}||`] : []),
              ...(language ? [`**Language:** ||${language}||`] : []),
            ].join("\n"),
            inline: false,
          },
        ],
        footer: {
          text: `JustPlugin Contact Form`,
        },
        timestamp: now.toISOString(),
      };

      const content = discordMention
        ? `New ticket from ${discordMention}`
        : undefined;

      try {
        const webhookRes = await fetch(webhookUrl, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            ...(content ? { content } : {}),
            embeds: [embed],
          }),
          signal: AbortSignal.timeout(5000),
        });

        if (!webhookRes.ok) {
          console.error(`Discord webhook failed: ${webhookRes.status}`);
          return NextResponse.json({ error: "Failed to deliver your message. Please try again." }, { status: 502 });
        }
      } catch {
        console.error("Discord webhook request failed");
        return NextResponse.json({ error: "Failed to deliver your message. Please try again later." }, { status: 502 });
      }
    }

    return NextResponse.json({ success: true });
  } catch (err) {
    console.error("Contact form error:", err);
    return NextResponse.json({ error: "Something went wrong. Please try again." }, { status: 500 });
  }
}
