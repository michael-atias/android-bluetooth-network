# Nearby P2P Messaging - Android

Android application developed as part of my Software Engineering studies to explore device-to-device communication with Google Nearby Connections.

The application uses a **star topology**: one device acts as the Master and other devices connect as Clients. Clients discover the Master, establish a Nearby connection, and exchange messages through it.

The chat interface is mainly a demonstration of the communication layer. The main focus of the project is discovery, connection management, message routing, permissions, and handling multiple nearby devices.

## Architecture

```text
             Master
          /     |     \
     Client A Client B Client C
```

The project uses Google Nearby Connections with:

```java
Strategy.P2P_STAR
```

### Master

The Master device:

- Advertises itself to nearby devices
- Accepts or rejects connection requests
- Maintains the connected-client list
- Receives messages from clients
- Broadcasts received messages to connected clients
- Handles client disconnects

### Client

A Client device:

- Searches for nearby Masters
- Displays discovered endpoints
- Requests a connection to a selected Master
- Confirms the authentication code shown by Nearby Connections
- Sends and receives messages
- Handles Master disconnection

## Technical Highlights

- Java
- Android SDK
- Google Nearby Connections
- `P2P_STAR` topology
- Device discovery and advertising
- Connection lifecycle handling
- Authentication-code confirmation
- Stream and byte payload handling
- Background threads for stream processing
- Message routing and broadcasting
- Bluetooth and location state/permission handling

## Message Flow

A typical message from a Client follows this path:

```text
Client
  |
  v
Master
  |
  +----> Client A
  +----> Client B
  +----> Client C
```

The Master therefore acts as the coordination point for the connected devices.

## Project Structure

```text
android-bluetooth-network/
├── app/
│   ├── src/main/
│   │   ├── java/com/michael/bluetooth/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── gradle/wrapper/
├── build.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle
```

## Building

The repository includes the Gradle Wrapper.

The project has been verified to build with:

- **JDK 17**
- **Gradle 7.4**
- **Android Gradle Plugin 7.3.1**
- **compileSdk 32**
- **minSdk 26**
- **targetSdk 32**

On Windows:

```powershell
.\gradlew.bat clean assembleDebug
```

On macOS/Linux:

```bash
./gradlew clean assembleDebug
```

A local `local.properties` file pointing to the Android SDK is required by the Android build environment and is intentionally excluded from version control.

## Running

For the full Nearby Connections flow, use Android devices that support the required nearby communication capabilities.

1. Launch the application on the devices.
2. Choose **Master** on one device.
3. Start advertising from the Master.
4. Choose **Client** on another device.
5. Start discovery and select the discovered Master.
6. Confirm the authentication code on both devices.
7. Send messages between the connected devices.

The application checks for Bluetooth/location availability and requests the permissions used by this version of Nearby Connections.

## Experimental HTTP / MongoDB Work

The repository also contains experimental classes for sending data to a local HTTP backend associated with MongoDB work from the project.

This code is separate from the core Nearby messaging flow and is not required to run the Master/Client communication.

## Background

This is an academic project from my Software Engineering studies. The source code is intentionally preserved close to the original implementation so the repository reflects the architecture and development approach I used at the time.
