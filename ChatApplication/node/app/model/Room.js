var seq = require('sequelize');

// set connection
var con = new seq('chat_application', 'root', '');

exports.connection = con;

var Room = con.define('rooms', {
	room_name 		: 	seq.STRING,
	user_1			: 	seq.INTEGER,
	user_1_socket	: seq.STRING,
	user_2			: 	seq.INTEGER,
	user_2_socket	: seq.STRING,
	created 		: 	seq.STRING,
	created_ip 		: 	seq.STRING,
	modified		: 	seq.STRING,
	modified_ip		: 	seq.STRING
},
{timestamps : false});

module.exports = (function(){

	var X = {};

	X.GetRooms = function(callback){
		Room.findAll({
			where:{
				$or:[
				  {user_1: null},
				  {user_2: null}
				]
			}
		}).done(function(results,err){
			if(err){
				callback({err:1});
			}else{
				callback({err:0,results:results});
			}
		})
	};
	X.CreateRoom = function(data,callback){
		Room.create(data).done(function(result,err){
			var success = (err) ? false : true;
			callback({success: success,result: result.dataValues});
		});
	}
	X.JoinRoom = function(data,callback){
		var updateData;
		if(data.user_2){
			updateData = {user_1: data.user_id,user_1_socket: data.socket};
			data.user_1 = data.user_id;
		}else{
			updateData = {user_2: data.user_id,user_2_socket: data.socket};
			data.user_2 = data.user_id;
		}
		Room.update(updateData,{
		  where: { id: data.id }
		}).then(function(result,err){
			var success = (err) ? false : true;
			callback({success: success,room: data});
		})
	}
	return X;

})();