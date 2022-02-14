# Motion Binding

Binding to integrate Motion devices.

## Supported Things

- `motionbridge`: Motion bridge
- `motionblind`: Regular blind

## Discovery

After configuring the bridge, connected devices can be discovered automatically. 

## Thing Configuration

### `motionbridge` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| secret          | text    | API key to access the bridge          | N/A     | yes      | no       |

### `motionblind` Thing Configuration

| Name            | Type    | Description               | Default | Required | Advanced |
|-----------------|---------|---------------------------|---------|----------|----------|
| macAddress      | text    | Mac address of the device | N/A     | yes      | no       |


### Full Example

Things:

```java
Bridge motion:motionbridge:abc [ hostname="192.168.1.42", secret="secret-key-12" ] {
    Thing motion:motionblind:abc:d267b25edad5aaa1 [ macAddress="d267b25edad5aaa1" ]
}
```

## Channels

| Channel | Type          | Read/Write | Description                 |
|---------|---------------|------------|-----------------------------|
| control | Rollershutter | RW         | This is the control channel |
