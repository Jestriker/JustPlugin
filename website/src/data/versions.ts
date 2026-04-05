export const VERSIONS = ["1.5", "1.4", "1.3", "1.2", "1.1", "1.0"] as const;
export const LATEST_VERSION = "1.5";
export type Version = (typeof VERSIONS)[number];

// Track which version added each command
export const commandVersions: Record<string, string> = {
  // v1.5 additions
  automessage: "1.5",
  // v1.4 additions
  jail: "1.4",
  unjail: "1.4",
  setjail: "1.4",
  deljail: "1.4",
  jails: "1.4",
  jailinfo: "1.4",
  kit: "1.4",
  kitpreview: "1.4",
  kitcreate: "1.4",
  kitedit: "1.4",
  kitrename: "1.4",
  kitdelete: "1.4",
  kitpublish: "1.4",
  kitdisable: "1.4",
  kitenable: "1.4",
  kitarchive: "1.4",
  kitlist: "1.4",
  afk: "1.4",
  mail: "1.4",
  nick: "1.4",
  tag: "1.4",
  tagcreate: "1.4",
  tagdelete: "1.4",
  taglist: "1.4",
  jpbackup: "1.4",
  tpoff: "1.4",
  getposoff: "1.4",
  getdeathposoff: "1.4",
  invseeoff: "1.4",
  echestseeoff: "1.4",
  // v1.3 additions
  skin: "1.3",
  skinban: "1.3",
  skinunban: "1.3",
  maintenance: "1.3",
  // Everything else is 1.0
};

// Same for permissions
export const permissionVersions: Record<string, string> = {
  "justplugin.automessage": "1.5",
  "justplugin.automessage.reload": "1.5",
  "justplugin.automessage.list": "1.5",
  "justplugin.automessage.toggle": "1.5",
  "justplugin.automessage.send": "1.5",
  "justplugin.jail": "1.4",
  "justplugin.unjail": "1.4",
  "justplugin.setjail": "1.4",
  "justplugin.deljail": "1.4",
  "justplugin.jails": "1.4",
  "justplugin.jailinfo": "1.4",
  "justplugin.kit": "1.4",
  "justplugin.afk": "1.4",
  "justplugin.mail": "1.4",
  "justplugin.nick": "1.4",
  "justplugin.tag": "1.4",
  "justplugin.backup": "1.4",
  "justplugin.skin": "1.3",
  "justplugin.maintenance": "1.3",
};
