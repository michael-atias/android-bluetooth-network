# Nearby P2P Messaging – Android Project

## Overview
Android application developed as part of my **Software Engineering diploma** to explore **peer-to-peer (P2P) communication** using **Google Nearby Connections**.  

The system enables multiple devices to discover each other, connect, and exchange messages in real time. **Chat is only a demonstration; the core goal is the P2P network**, which can be extended for any type of device-to-device communication.

## Architecture

           [ Master Device ]
                 |
    ---------------------------------
    |               |               |
 [ Client A ]    [ Client B ]    [ Client C ]

- Star topology (Master coordinates all clients)  
- Clients send messages to Master; Master broadcasts to all  
- Uses Nearby Connections API (Bluetooth/Wi-Fi)  

## Features
- Master–Client role selection  
- Device discovery and connection management  
- Real-time chat (as a demonstration of the network)  
- Streamed message handling with background threads  
- Message log and client list  
- Permission handling (Bluetooth & Location)  
- Optional MongoDB integration (store messages/clients)  

## Key Concepts Implemented
- Google Nearby Connections API  
- Bluetooth & Location permission handling  
- Master–Client network logic  
- Real-time message broadcasting  
- Serialization of messages with timestamps  
- Background thread handling for streams  

## Running the Project
1. Start the **Master** activity on one device  
2. Start **Client** activity on other devices  
3. Clients discover Master and request connections  
4. Connected devices exchange messages  


## Limitations
- Focused on learning objectives, not production-ready  
- Basic error handling and input validation  
- Minor UI/network edge-case bugs  

## License
- Provided for **educational purposes only**
