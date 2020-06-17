# Chat Server beta

## Description

This is a multi-threaded server-client based messaging platform that supports multiple users at once. It is coded in Java and concepts 
such as multi-threading, socket programming and network programming are implemented. A server can be created by a host with specific 
port number and network address. Once created, the server will keep runnning until the host terminates the program. Users can join the 
server by creating a unique name, entering the port number and the network address.

## Purpose

This chat server is a school project, the objective for creating this application is to gain a better understanding and gain practial 
experience on concepts such as multi-threading, socket programming and network programming. 

To further improve this application, I plan to create a better user interface using Javascript. Currently, this program does not have any UI/UX design, all operations are done using command prompt.

## Features

Username filtering:
The server can filter usernames by performing username checking before a user joins the server. As the user enters a username, the 
server will run through a list of existing users; if an identical username already exists, then the server will prompt the user to enter 
a new username.

Message broadcasting:
By default, when a user joins a server, all messages are broadcasted in the server. 

Direct messaging:
Users are allowed to perform direct messaging by entering "/msg" followed by a desired username, then by the message. (e.g. /msg user1 
Hello there!)

User listing:
If a user would like to know who is in the server, the server can list down all existing users as per user's command ("/list").

Message filtering:
The server is capable of filtering messages that contains inappropriate words. The host will have to supply a file that contains the 
inappropriate words. The program will then read the data file and detect the words and replace them with astrids before the message is 
broadcasted or direct messaged.

## Demo


