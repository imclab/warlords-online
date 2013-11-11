var Data = require("./game_data");

module.exports = {

	createMap: function (type, armies){
		console.log('Creating new '.info + armies.length + '-players map, type '.info + type + ', armies : '.info + armies);
		switch (type){
			case 0 :
				return createStandardMap(armies); 
			break;
			case 1 :
				return createRandomMap(armies); 
			break;
		}
	}

};

function createStandardMap(armies){
	var map;
	switch (armies.length){
		case 2 :
			map = buildTerrain(standardMap2);
			addStartPosition(map, 1, 1, 0, armies[0]);
			addStartPosition(map, 3, 3, 1, armies[1]);
		break;
		case 3 :
			map = buildTerrain(standardMap3);
			addStartPosition(map, 1, 3, 0, armies[0]);
			addStartPosition(map, 3, 1, 1, armies[1]);
			addStartPosition(map, 4, 4, 2, armies[2]);
		break;
		case 4 :
			map = buildTerrain(standardMap4);
			addStartPosition(map, 1, 1, 0, armies[0]);
			addStartPosition(map, 5, 5, 1, armies[1]);
			addStartPosition(map, 1, 5, 2, armies[2]);
			addStartPosition(map, 5, 1, 3, armies[3]);
		break;
		case 8 :
			map = buildTerrain(standardMap8);
			addStartPosition(map, 1, 1, 0, armies[0]);
			addStartPosition(map, 4, 1, 1, armies[1]);
			addStartPosition(map, 7, 1, 2, armies[2]);
			addStartPosition(map, 1, 4, 3, armies[3]);
			addStartPosition(map, 7, 4, 4, armies[4]);
			addStartPosition(map, 1, 7, 5, armies[5]);
			addStartPosition(map, 4, 7, 6, armies[6]);
			addStartPosition(map, 7, 7, 7, armies[7]);
		break;
	}
	return map;
}

function createRandomMap(armies){
	console.log('Building random terrain '.debug + ' for '.debug + armies.length + ' players...'.debug);
	var map;
	
	switch(armies.length){
		case 2:
			//create rough map
			map = buildRandomMap(6);
			//adds players zone
			addPlayerZoneToMap(map, 0, 0);
			addPlayerZoneToMap(map, 3, 3);
		break;
		case 3:
			//create rough map
			map = buildRandomMap(7);
			//adds players zone
			addPlayerZoneToMap(map, 0, 0);
			addPlayerZoneToMap(map, 4, 0);
			addPlayerZoneToMap(map, 2, 4);
		break;
		case 4:
			//create rough map
			map = buildRandomMap(9);
			//adds players zone
			addPlayerZoneToMap(map, 0, 3);
			addPlayerZoneToMap(map, 6, 3);
			addPlayerZoneToMap(map, 3, 0);
			addPlayerZoneToMap(map, 3, 6);
		break;
		case 8:
			//create rough map
			map = buildRandomMap(11);
			//adds players zone
			addPlayerZoneToMap(map, 0, 0);
			addPlayerZoneToMap(map, 4, 0);
			addPlayerZoneToMap(map, 8, 0);
			addPlayerZoneToMap(map, 0, 4);
			addPlayerZoneToMap(map, 8, 4);
			addPlayerZoneToMap(map, 0, 8);
			addPlayerZoneToMap(map, 4, 8);
			addPlayerZoneToMap(map, 8, 8);
		break;
	}	
	
	//add start positions
	var n = 0;
	for(var j = 0; j < map.length; j++){
		for(var i = 0; i < map[0].length; i++){
			if(map[j][i].type == 1){
				addStartPosition(map, j, i, n, armies[n]);
				n++;
			}
		}
	}
	return map;
}

function buildRandomMap(mapSize){
	var map = [];
	for(var j = 0; j < mapSize; j++){
		map[j] = [];
		for(var i = 0; i < mapSize; i++){
			map[j][i] = new Data.TerrainTile(getRandomTile(), i, j);		
		}
	}
	return map;
}

function addPlayerZoneToMap(map, x, y){
	var zone = createOnePlayerZone();
	var n = 0;
	for(var j = 0; j < 3; j++){
		for(var i = 0; i < 3; i++){
			map[y + j][x + i] = new Data.TerrainTile(zone[n], i + x, j + y);
			n++;
		}
	}
}

function createOnePlayerZone(){
	var zone = [];
	for(var n = 0; n < 9; n++){
		zone.push(getRandomNormalTile());
	}
	//add farms
	zone[parseInt(Math.random() * 9)] = 2;
	if(Math.random() < 0.4){
		zone[parseInt(Math.random() * 9)] = 2;
	}
	//add fort
	if(Math.random() < 0.4){
		zone[parseInt(Math.random() * 9)] = 5;
	}
	//add castle
	zone[parseInt(Math.random() * 9)] = 1;
	return zone;
}

function getRandomNormalTile(){
	var n = Math.random();
	if(n < 0.70){
		return 0;
	} else if(n < 0.72){
		return 6;
	} else if(n < 0.8){
		return 7;
	} else {
		return 3;
	}
}

function getRandomTile(){
	var n = Math.random();
	if(n < 0.6){
		return 0;
	} else if(n < 0.62){
		return 6;
	} else if(n < 0.7){
		return 7;
	} else if(n < 0.85){
		return 3;
	} else if(n < 0.95){
		return 2;
	} else{
		return 5;
	}
}

function isEnemyAround(map, position, lackSpace){
	if(position[0] - 1 >= 0){
		if(position[1] - 1 >= 0 && map[position[0] - 1][position[1] - 1].type == 1 && !lackSpace
			|| map[position[0] - 1][position[1]].type == 1
		|| position[1] + 1 < map.length && map[position[0] - 1][position[1] + 1].type == 1 && !lackSpace){
			return true;
		}
	}
	if(position[0] + 1 < map.length){
		if(position[1] - 1 >= 0 && map[position[0] + 1][position[1] - 1].type == 1 && !lackSpace
			|| map[position[0] + 1][position[1]].type == 1
		|| position[1] + 1 < map.length && map[position[0] + 1][position[1] + 1].type == 1 && !lackSpace){
			return true;
		}
	}
	if(position[1] - 1 >= 0 && map[position[0]][position[1] - 1].type == 1
			|| map[position[0]][position[1]].type == 1
		|| position[1] + 1 < map.length && map[position[0]][position[1] + 1].type == 1){
			return true;
	}
	return false;
}

function buildTerrain(mapDescription){
	console.log('Building terrain... '.debug + mapDescription.length + ' x '.debug + mapDescription[0].length + ' tiles'.debug);
	var map = [];
	for(var j = 0; j < mapDescription.length; j++){
		map[j] = [];
		for(var i = 0; i < mapDescription[0].length; i++){
			map[j][i] = new Data.TerrainTile(mapDescription[j][i], i, j);		
		}
	}
	return map;
}

function addStartPosition(map, x, y, owner, army){
	console.log('New start position here : '.debug + x + ',' + y + ', army = '.debug + army);
	map[x][y].owner = owner;
	switch(army){
		case 0: 
			map[x][y].content.push(new Data.Infantry(owner));
			map[x][y].content.push(new Data.Infantry(owner));
		break;
		case 1: 
			map[x][y].content.push(new Data.Skeleton(owner));
			map[x][y].content.push(new Data.Skeleton(owner));
		break;
		case 2: 
			map[x][y].content.push(new Data.ChaosWarriors(owner));
			map[x][y].content.push(new Data.ChaosWarriors(owner));
		break;
		case 3: 
			map[x][y].content.push(new Data.Goblins(owner));
			map[x][y].content.push(new Data.Goblins(owner));
		break;
	}
}

var standardMap2 = [
		[0, 0, 2, 0, 2],
		[2, 1, 3, 3, 0],
		[3, 0, 3, 0, 3],
		[0, 3, 3, 1, 2],
		[5, 0, 2, 0, 0]];

var standardMap3 = [
		[3, 5, 0, 0, 2, 0],
		[0, 0, 3, 1, 3, 0],
		[3, 0, 3, 0, 0, 5],
		[0, 1, 0, 2, 3, 2],
		[2, 0, 3, 0, 1, 0],
		[0, 3, 5, 3, 0, 0]];

var standardMap4 = [
		[0, 2, 0, 2, 0, 0, 0],
		[0, 1, 3, 3, 0, 1, 2],
		[3, 0, 0, 3, 3, 0, 0],
		[2, 3, 0, 5, 0, 3, 2],
		[0, 0, 3, 3, 0, 0, 0],
		[2, 1, 0, 3, 0, 1, 0],
		[3, 3, 0, 2, 3, 2, 3]];

var standardMap8 = [
		[0, 2, 3, 0, 2, 3, 0, 0, 3],
		[0, 1, 0, 3, 1, 0, 3, 1, 2],
		[3, 0, 3, 0, 3, 0, 0, 0, 3],
		[3, 3, 0, 5, 0, 5, 0, 3, 0],
		[2, 1, 3, 0, 2, 0, 3, 1, 2],
		[3, 0, 0, 5, 0, 5, 0, 0, 0],
		[0, 3, 0, 3, 0, 3, 0, 3, 3],
		[2, 1, 3, 0, 1, 0, 3, 1, 0],
		[3, 0, 3, 0, 2, 0, 0, 2, 3]];
