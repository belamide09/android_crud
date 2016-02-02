// Require modules
var express     = require('express');
var multer      = require('multer');
var qs          = require('querystring');
var app         = express();
var mysql       = require("mysql");
var http        = require('http').Server(app);
var io          = require("socket.io")(http);
var router      = express.Router();
var getIP       = require('ipware')().get_ip;
var bodyParser  = require('body-parser');
var requestIp   = require('request-ip');
var dateFormat  = require('dateformat');
var bodyParser  = require('body-parser');
var upload      = multer();
var port 				= 3000;

var Room        = require('./app/model/Room');

io.on('connection',function(socket) {
	socket.on('get_rooms',function(data){
		Room.GetRooms(function(results){
			io.to(socket.id).emit('returnRooms',results['results']);
		});
	});
	socket.on('create_room',function(data){
		var saveData = {
			room_name: data.room_name,
			user_1: data.user_id,
			user_1_socket: socket.id,
			user_2: null,
			created: today(),
			created_ip: getIp(socket),
			modified: today(),
			modified_ip: getIp(socket)
		};
		Room.CreateRoom(saveData,function(data){
			io.to(socket.id).emit('responseCreateRoom',data);
			io.emit('UpdateRoom',data);
			io.emit('AppendRoom',data);
		})
	});
	socket.on('join_room',function(data){
		data.socket = socket.id;
		Room.JoinRoom(data,function(result){
			io.to(socket.id).emit('responseJoinRoom',result);
			io.emit('RemoveRoom',result.room);
		})
	});
	socket.on('send_message',function(data){
		io.emit('ReceiveMessage',data);
	});

});

var getIp = function(socket){
    var socketId  = socket.id;
    var clientIp  = socket.request.connection.remoteAddress;
    return clientIp;
};

var today = function(){
return dateFormat(new Date(), 'yyyy-mm-dd hh:mm:ss');
};

app.use('/', router);
http.listen(port,function(){
  console.log("Listening on "+port);
});

