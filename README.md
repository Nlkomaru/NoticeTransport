# NoticeTransport

Transfers players to other servers

## Config example
```plugins/noticetransport/config.conf```
```hocon
message = 順番が来ました
templateFileName {
    "event": "test"
}
timeOut = 30
ver = "1.0"
```

## Template file example 
```plugins/noticetransport/location/<fileName>.json```
```json
{
  "server": "lobby",
  "world": "world",
  "x": 0,
  "y": 128,
  "z": 0
}
```

## Initialize teleport example 
```plugins/Noticetransport/init.json```
```json
{
  "world": "world",
  "x": 0,
  "y": 128,
  "z": 0
}
```

## Commands

command   :```nt tp -d <playerName> <serverName> <world> <x> <y> <z> ```<br>
permission: ```noticetransport.commands.transport```<br>
指定した座標にプレイヤーを飛ばします

command   :```nt tp -t <playerName> <file>```<br>
permission: ```noticetransport.commands.transport```<br>
テンプレートファイルに設定された座標にプレイヤーを飛ばします

command   :```nt wait```<br>
permission: ```noticetransport.commands.wait```<br>
順番待ちをします

command   :```nt invite <playerName>```<br>
permission: ```noticetransport.commands.invite```<br>
ほかの人を招待します

command   :```nt clear -w```<br>
permission: ```noticetransport.commands.clear.wait```<br>
待っている人を削除します

command   : ```nt clear -p <serverName>```<br>
permission: ```noticetransport.commands.clear.playing```<br>
指定したサーバーでplaying状態を削除します

command   :```nt show -w```<br>
permission: ```noticetransport.commands.show.wait```<br>
待っている人を表示します

command   :```nt show -p <serverName>```<br>
permission: ```noticetransport.commands.show.playing```<br>
サーバーでplaying状態になっている人を表示します

command   :```nt template <fileName> <serverName> <world> <x> <y> <z>```<br>
permission: ```noticetransport.commands.create.template```<br>
テンプレートファイルを作成します

command   :```nt tp wait accept <serverName>```<br>
permission: ```noticetransport.commands.tp.accept```<br>
招待(ほかの人からの招待や順番待ちを含む)を承認します

