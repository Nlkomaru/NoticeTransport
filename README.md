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
