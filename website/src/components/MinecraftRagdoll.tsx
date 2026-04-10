"use client";

import { useEffect, useRef } from "react";
import { SkinViewer } from "skinview3d";

interface Props {
  username: string;
  className?: string;
}

// Per-limb angular physics
interface LimbPhysics {
  angle: number;     // current rotation (radians)
  vel: number;       // angular velocity
  rest: number;      // resting angle (standing pose)
  min: number;       // joint limit min
  max: number;       // joint limit max
  inertia: number;   // how much body acceleration affects this limb
  gravity: number;   // how much gravity pulls the limb down
  spring: number;    // spring force toward rest pose
  damping: number;   // angular damping
}

function makeLimb(opts: Partial<LimbPhysics> & { rest: number; min: number; max: number }): LimbPhysics {
  return {
    angle: opts.rest,
    vel: 0,
    rest: opts.rest,
    min: opts.min,
    max: opts.max,
    inertia: opts.inertia ?? 0.08,
    gravity: opts.gravity ?? 0.012,
    spring: opts.spring ?? 0.02,
    damping: opts.damping ?? 0.92,
  };
}

export default function MinecraftRagdoll({ username, className }: Props) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const viewerRef = useRef<SkinViewer | null>(null);
  const physRef = useRef<{
    // Body position & velocity
    x: number; y: number;
    vx: number; vy: number;
    // Previous velocity (for acceleration calc)
    pvx: number; pvy: number;
    // Body tilt
    bodyTilt: number; bodyTiltVel: number;
    // Limbs — rotation around X axis (swing forward/back)
    head: LimbPhysics;
    rArmX: LimbPhysics;  // right arm forward/back
    rArmZ: LimbPhysics;  // right arm in/out
    lArmX: LimbPhysics;
    lArmZ: LimbPhysics;
    rLegX: LimbPhysics;
    lLegX: LimbPhysics;
    // Drag
    dragging: boolean;
    grabX: number; grabY: number;
    mouseX: number; mouseY: number;
    hist: { x: number; y: number; t: number }[];
    // Bounds
    bL: number; bR: number; bT: number; bB: number;
    px: number; // pixels per world unit
    // Grounded
    grounded: boolean;
    groundedTime: number;
    frame: number;
  } | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const w = canvas.clientWidth;
    const h = canvas.clientHeight;

    const viewer = new SkinViewer({
      canvas, width: w, height: h,
      skin: `https://mc-heads.net/skin/${encodeURIComponent(username)}`,
    });

    viewer.fov = 50;
    viewer.zoom = 1;
    viewer.autoRotate = false;
    viewer.controls.enableRotate = false;
    viewer.controls.enableZoom = false;
    viewer.controls.enablePan = false;
    viewer.background = null;
    viewer.globalLight.intensity = 2.8;
    viewer.cameraLight.intensity = 0.6;

    // DISABLE built-in animation — we drive limbs ourselves
    viewer.animation = null;

    viewerRef.current = viewer;

    // World bounds
    const fovRad = (50 * Math.PI) / 180;
    const camZ = 65;
    const halfH = Math.tan(fovRad / 2) * camZ;
    const halfW = halfH * (w / h);
    const charW = 18, charTop = 20, charBot = 22;

    const p: NonNullable<typeof physRef.current> = {
      x: 0, y: -halfH + charBot, vx: 0, vy: 0,
      pvx: 0, pvy: 0,
      bodyTilt: 0, bodyTiltVel: 0,

      // Head: nods forward/back
      head: makeLimb({ rest: 0, min: -0.8, max: 0.6, inertia: 0.12, gravity: 0.008, spring: 0.06, damping: 0.88 }),

      // Right arm: swings freely
      rArmX: makeLimb({ rest: 0, min: -2.8, max: 2.8, inertia: 0.15, gravity: 0.02, spring: 0.01, damping: 0.93 }),
      rArmZ: makeLimb({ rest: 0, min: -0.3, max: 1.8, inertia: 0.06, gravity: 0.015, spring: 0.008, damping: 0.93 }),

      // Left arm
      lArmX: makeLimb({ rest: 0, min: -2.8, max: 2.8, inertia: 0.15, gravity: 0.02, spring: 0.01, damping: 0.93 }),
      lArmZ: makeLimb({ rest: 0, min: -1.8, max: 0.3, inertia: 0.06, gravity: -0.015, spring: 0.008, damping: 0.93 }),

      // Legs: swing forward/back, more restricted
      rLegX: makeLimb({ rest: 0, min: -1.6, max: 1.8, inertia: 0.12, gravity: 0.015, spring: 0.015, damping: 0.91 }),
      lLegX: makeLimb({ rest: 0, min: -1.6, max: 1.8, inertia: 0.12, gravity: 0.015, spring: 0.015, damping: 0.91 }),

      dragging: false, grabX: 0, grabY: 0,
      mouseX: 0, mouseY: 0,
      hist: [],
      bL: -halfW + charW, bR: halfW - charW,
      bT: halfH - charTop, bB: -halfH + charBot,
      px: h / (halfH * 2),
      grounded: true, groundedTime: 0,
      frame: 0,
    };
    physRef.current = p;

    // Physics constants
    const GRAVITY = 0.55;
    const BOUNCE = 0.5;
    const AIR_DRAG = 0.998;
    const FLOOR_FRIC = 0.85;
    const SETTLE = 0.6;

    function updateLimb(limb: LimbPhysics, bodyAccX: number, bodyAccY: number, grounded: boolean, groundedTime: number, bodyRot: number) {
      // Inertia: body acceleration causes limb to swing opposite
      // For X rotation (forward/back), vertical acceleration swings forward, horizontal doesn't
      // We use a combo of both axes
      const inertialForce = (-bodyAccY * 0.7 + bodyAccX * 0.5) * limb.inertia;
      limb.vel += inertialForce;

      // Gravity on the limb (pendulum effect, modified by body tilt)
      limb.vel += Math.sin(limb.angle + bodyRot) * limb.gravity;

      // Spring toward rest pose — stronger when grounded and settled
      const springMul = grounded ? Math.min(1, groundedTime / 30) * 3 + 1 : 1;
      limb.vel += (limb.rest - limb.angle) * limb.spring * springMul;

      // Damping — more when grounded
      const dampMul = grounded ? limb.damping * 0.95 : limb.damping;
      limb.vel *= dampMul;

      // Integrate
      limb.angle += limb.vel;

      // Joint limits with bounce
      if (limb.angle < limb.min) {
        limb.angle = limb.min;
        limb.vel *= -0.3;
      }
      if (limb.angle > limb.max) {
        limb.angle = limb.max;
        limb.vel *= -0.3;
      }
    }

    function step() {
      const s = physRef.current;
      const v = viewerRef.current;
      if (!s || !v) return;

      // Store previous velocity for acceleration
      s.pvx = s.vx;
      s.pvy = s.vy;

      if (s.dragging) {
        // Direct follow
        const tx = s.mouseX + s.grabX;
        const ty = s.mouseY + s.grabY;
        s.vx = tx - s.x;
        s.vy = ty - s.y;
        s.x = Math.max(s.bL, Math.min(s.bR, tx));
        s.y = Math.max(s.bB, Math.min(s.bT, ty));
        s.grounded = false;
        s.groundedTime = 0;
      } else {
        s.vy -= GRAVITY;
        s.vx *= AIR_DRAG;
        s.vy *= AIR_DRAG;
        s.x += s.vx;
        s.y += s.vy;

        s.grounded = false;

        // Floor
        if (s.y < s.bB) {
          s.y = s.bB;
          if (s.vy < 0) {
            s.vy *= -BOUNCE;
            if (Math.abs(s.vy) < SETTLE) s.vy = 0;
          }
          s.vx *= FLOOR_FRIC;
          s.grounded = true;
        }
        // Ceiling
        if (s.y > s.bT) {
          s.y = s.bT;
          if (s.vy > 0) { s.vy *= -BOUNCE; if (Math.abs(s.vy) < SETTLE) s.vy = 0; }
        }
        // Walls
        if (s.x < s.bL) {
          s.x = s.bL;
          if (s.vx < 0) { s.vx *= -BOUNCE; if (Math.abs(s.vx) < SETTLE) s.vx = 0; }
        }
        if (s.x > s.bR) {
          s.x = s.bR;
          if (s.vx > 0) { s.vx *= -BOUNCE; if (Math.abs(s.vx) < SETTLE) s.vx = 0; }
        }

        if (s.grounded) {
          s.groundedTime++;
        } else {
          s.groundedTime = 0;
        }
      }

      // Body acceleration (change in velocity = force felt by limbs)
      const accX = s.vx - s.pvx;
      const accY = s.vy - s.pvy;

      // Body tilt — leans in direction of horizontal movement
      const tiltTarget = s.grounded ? -s.vx * 0.02 : -s.vx * 0.04 + Math.sin(s.bodyTilt) * 0.01;
      s.bodyTiltVel += (tiltTarget - s.bodyTilt) * 0.08;
      s.bodyTiltVel *= 0.88;
      s.bodyTilt += s.bodyTiltVel;
      // Clamp body tilt
      s.bodyTilt = Math.max(-0.5, Math.min(0.5, s.bodyTilt));

      // Update all limbs
      updateLimb(s.head, accX, accY, s.grounded, s.groundedTime, s.bodyTilt);
      updateLimb(s.rArmX, accX, accY, s.grounded, s.groundedTime, s.bodyTilt);
      updateLimb(s.lArmX, accX, accY, s.grounded, s.groundedTime, s.bodyTilt);
      updateLimb(s.rLegX, accX, accY, s.grounded, s.groundedTime, s.bodyTilt);
      updateLimb(s.lLegX, accX, accY, s.grounded, s.groundedTime, s.bodyTilt);

      // Arm Z (sideways) — driven by vertical acceleration mostly
      const armZAcc = -accY * 0.3 + accX * 0.15;
      s.rArmZ.vel += armZAcc * s.rArmZ.inertia;
      s.rArmZ.vel += Math.sin(s.rArmZ.angle) * s.rArmZ.gravity;
      const armZSpring = s.grounded ? s.rArmZ.spring * (1 + Math.min(s.groundedTime / 30, 1) * 3) : s.rArmZ.spring;
      s.rArmZ.vel += (s.rArmZ.rest - s.rArmZ.angle) * armZSpring;
      s.rArmZ.vel *= s.rArmZ.damping;
      s.rArmZ.angle += s.rArmZ.vel;
      if (s.rArmZ.angle < s.rArmZ.min) { s.rArmZ.angle = s.rArmZ.min; s.rArmZ.vel *= -0.3; }
      if (s.rArmZ.angle > s.rArmZ.max) { s.rArmZ.angle = s.rArmZ.max; s.rArmZ.vel *= -0.3; }

      s.lArmZ.vel += (-armZAcc) * s.lArmZ.inertia;
      s.lArmZ.vel += Math.sin(s.lArmZ.angle) * s.lArmZ.gravity;
      const lArmZSpring = s.grounded ? s.lArmZ.spring * (1 + Math.min(s.groundedTime / 30, 1) * 3) : s.lArmZ.spring;
      s.lArmZ.vel += (s.lArmZ.rest - s.lArmZ.angle) * lArmZSpring;
      s.lArmZ.vel *= s.lArmZ.damping;
      s.lArmZ.angle += s.lArmZ.vel;
      if (s.lArmZ.angle < s.lArmZ.min) { s.lArmZ.angle = s.lArmZ.min; s.lArmZ.vel *= -0.3; }
      if (s.lArmZ.angle > s.lArmZ.max) { s.lArmZ.angle = s.lArmZ.max; s.lArmZ.vel *= -0.3; }

      // Walking animation when grounded and moving
      const speed = Math.abs(s.vx);
      if (s.grounded && speed > 0.3) {
        const walkFreq = Math.min(speed * 0.4, 4);
        const walkAmp = Math.min(speed * 0.08, 0.6);
        const t = performance.now() * 0.006 * walkFreq;
        // Push legs toward walking cycle
        s.rLegX.vel += (Math.sin(t) * walkAmp - s.rLegX.angle) * 0.15;
        s.lLegX.vel += (Math.sin(t + Math.PI) * walkAmp - s.lLegX.angle) * 0.15;
        // Arms swing opposite to legs
        s.rArmX.vel += (Math.sin(t + Math.PI) * walkAmp * 0.7 - s.rArmX.angle) * 0.08;
        s.lArmX.vel += (Math.sin(t) * walkAmp * 0.7 - s.lArmX.angle) * 0.08;
      }

      // Idle breathing when settled
      if (s.grounded && speed < 0.3 && s.groundedTime > 30) {
        const breathe = performance.now() * 0.002;
        const breathAmp = 0.03;
        s.rArmZ.vel += (Math.sin(breathe) * breathAmp - s.rArmZ.angle) * 0.02;
        s.lArmZ.vel += (-Math.sin(breathe) * breathAmp - s.lArmZ.angle) * 0.02;
        s.head.vel += (Math.sin(breathe * 0.7) * 0.02 - s.head.angle) * 0.01;
      }

      // Apply to skinview3d
      v.playerObject.position.x = s.x;
      v.playerObject.position.y = s.y;

      // Body tilt
      v.playerObject.rotation.z = s.bodyTilt;

      // Limb rotations
      const skin = v.playerObject.skin;
      skin.head.rotation.x = s.head.angle;
      skin.rightArm.rotation.x = s.rArmX.angle;
      skin.rightArm.rotation.z = s.rArmZ.angle;
      skin.leftArm.rotation.x = s.lArmX.angle;
      skin.leftArm.rotation.z = s.lArmZ.angle;
      skin.rightLeg.rotation.x = s.rLegX.angle;
      skin.leftLeg.rotation.x = s.lLegX.angle;

      s.frame = requestAnimationFrame(step);
    }

    p.frame = requestAnimationFrame(step);

    // --- Interaction ---
    function toWorld(cx: number, cy: number) {
      const s = physRef.current;
      if (!s || !canvas) return { wx: 0, wy: 0 };
      const rect = canvas.getBoundingClientRect();
      return {
        wx: (cx - rect.left - rect.width / 2) / s.px,
        wy: -(cy - rect.top - rect.height / 2) / s.px,
      };
    }

    function hitTest(wx: number, wy: number) {
      const s = physRef.current;
      if (!s) return false;
      return Math.abs(wx - s.x) < charW && Math.abs(wy - s.y) < Math.max(charTop, charBot);
    }

    function onDown(e: MouseEvent | TouchEvent) {
      const s = physRef.current;
      if (!s) return;
      const pt = "touches" in e ? e.touches[0] : e;
      const { wx, wy } = toWorld(pt.clientX, pt.clientY);
      if (hitTest(wx, wy)) {
        e.preventDefault();
        s.dragging = true;
        s.grabX = s.x - wx;
        s.grabY = s.y - wy;
        s.mouseX = wx;
        s.mouseY = wy;
        s.hist = [{ x: wx, y: wy, t: performance.now() }];
        s.vx = 0; s.vy = 0;
        if (canvas) canvas.style.cursor = "grabbing";
      }
    }

    function onMove(e: MouseEvent | TouchEvent) {
      const s = physRef.current;
      if (!s) return;
      const pt = "touches" in e ? e.touches[0] : e;
      const { wx, wy } = toWorld(pt.clientX, pt.clientY);
      if (s.dragging) {
        e.preventDefault();
        s.mouseX = wx;
        s.mouseY = wy;
        const now = performance.now();
        s.hist.push({ x: wx, y: wy, t: now });
        if (s.hist.length > 8) s.hist.shift();
      } else if (canvas) {
        canvas.style.cursor = hitTest(wx, wy) ? "grab" : "default";
      }
    }

    function onUp() {
      const s = physRef.current;
      if (!s || !s.dragging) return;
      s.dragging = false;

      // Throw velocity from mouse history
      const hist = s.hist;
      const now = performance.now();
      if (hist.length >= 2) {
        let oldest = hist[0];
        for (let i = 0; i < hist.length - 1; i++) {
          if (now - hist[i].t < 120) { oldest = hist[i]; break; }
        }
        const newest = hist[hist.length - 1];
        const dt = Math.max(0.5, (newest.t - oldest.t) / 16.67);
        const scale = 2.8;
        s.vx = ((newest.x - oldest.x) / dt) * scale;
        s.vy = ((newest.y - oldest.y) / dt) * scale;
        const maxV = 28;
        const spd = Math.sqrt(s.vx * s.vx + s.vy * s.vy);
        if (spd > maxV) { s.vx = (s.vx / spd) * maxV; s.vy = (s.vy / spd) * maxV; }

        // Give limbs a kick from the throw
        const kick = spd * 0.02;
        s.rArmX.vel += s.vy * 0.08;
        s.lArmX.vel += s.vy * 0.08;
        s.rArmZ.vel += kick;
        s.lArmZ.vel -= kick;
        s.rLegX.vel -= s.vy * 0.06;
        s.lLegX.vel -= s.vy * 0.06;
        s.head.vel += s.vy * 0.05;
      }
      s.hist = [];
      if (canvas) canvas.style.cursor = "default";
    }

    canvas.addEventListener("mousedown", onDown);
    window.addEventListener("mousemove", onMove);
    window.addEventListener("mouseup", onUp);
    canvas.addEventListener("touchstart", onDown, { passive: false });
    window.addEventListener("touchmove", onMove, { passive: false });
    window.addEventListener("touchend", onUp);

    const ro = new ResizeObserver(() => {
      if (!canvas || !physRef.current) return;
      const nw = canvas.clientWidth, nh = canvas.clientHeight;
      viewer.width = nw; viewer.height = nh;
      const nHH = Math.tan(fovRad / 2) * camZ;
      const nHW = nHH * (nw / nh);
      physRef.current.bL = -nHW + charW;
      physRef.current.bR = nHW - charW;
      physRef.current.bT = nHH - charTop;
      physRef.current.bB = -nHH + charBot;
      physRef.current.px = nh / (nHH * 2);
    });
    ro.observe(canvas);

    return () => {
      if (physRef.current) cancelAnimationFrame(physRef.current.frame);
      ro.disconnect();
      canvas.removeEventListener("mousedown", onDown);
      window.removeEventListener("mousemove", onMove);
      window.removeEventListener("mouseup", onUp);
      canvas.removeEventListener("touchstart", onDown);
      window.removeEventListener("touchmove", onMove);
      window.removeEventListener("touchend", onUp);
      viewer.dispose();
      viewerRef.current = null;
      physRef.current = null;
    };
  }, [username]);

  return <canvas ref={canvasRef} className={className} style={{ touchAction: "none" }} />;
}
