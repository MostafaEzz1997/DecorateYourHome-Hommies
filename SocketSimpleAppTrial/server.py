import socket
import sys
from _thread import *
import struct
host = "192.168.56.1"
port = 9999
s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)

try:
    s.bind((host,port))
except socket.error as e:
    print(str(e))

s.listen(5) #Enable a server to accept connections.
print("Waiting for a connection...")


client, addr = s.accept()
print ('got connected from', addr)

buf = ""
while len(buf)<4:
    buf += client.recv(4-len(buf))
size = struct.unpack('!i', buf)
print ("receiving %s bytes" % size)

with open('tst.jpg', 'wb') as img:
    while True:
        data = client.recv(1024)
        if not data:
            break
        img.write(data)
print ('received, yay!')

client.close()


"""
string =""
def threaded_client(conn):
    conn.send(str.encode("Welcome\n"))
    global string
    string = ""
    while True:
    	# for m in range (0,20): #Disconnects after x chars
	    data = conn.recv(1) #Receive data from the socket.

	    if not data:
	        reply = "Server output: "+ string
	        conn.sendall(str.encode(reply))
	        break
	    else:
	        string += data.decode("utf-8")
    conn.close()
		

while True:
	conn, addr = s.accept()
	start_new_thread(threaded_client,(conn,))
	if len(string)>1:
		print("connected to: "+addr[0]+":"+str(addr[1]))
		print("message is : "+string)
	##print(string)
"""
    

    
    
    

	
	