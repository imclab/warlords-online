var Data = require("./game_data")

module.exports = {

	getAIOrders: function (players, playerId, gold, gameData, army, AIType){
		console.log('Getting orders from AI...'.info);
		return getOrdersFromAI(players, playerId, gold, gameData, army, AIType);
	}

};

function getOrdersFromAI(players, playerId, gold, gameData, army, AIType){
	//new random bot each turn
	if(AIType.name == 'random'){
		AIType = new Data.RandomBot();
	}
	//get dead players
	var deadPlayers = [];
	for(var i in players){
		deadPlayers.push(players[i]);
	}
	for(var j = 0; j < gameData.terrain.length; j++){
		for(var i = 0; i < gameData.terrain[0].length; i++){
			if(gameData.terrain[j][i].type == 1
				&& gameData.terrain[j][i].owner >= 0){
				if(deadPlayers.indexOf(players[gameData.terrain[j][i].owner]) >= 0){
					deadPlayers.splice(deadPlayers.indexOf(players[gameData.terrain[j][i].owner]), 1);
				}
			}
		}
	}

	var orders = [];

	var securityThreshold = AIType.securityThreshold;
	var attackThreshold = AIType.attackThreshold;

 	//get player correct owner ID
	var ownerId;
	for(var i in players){
		if(players[i] == playerId){
			ownerId = i;
			break;
		}
	}
	console.log('Player with ownerId : '.info + ownerId + ' is away so AI will replace...'.info);
	//get relevant data from map : armies, buildings near, income...
	var castles = [];
	var strongPoints = [];
	var myArmies = [];
	var enemiesAround = [];
	var myBuildings = [];
	var buildingsAround = [];
	var income = 0;
	var supply = 0;
	for(var j = 0; j < gameData.terrain.length; j++){
		for(var i = 0; i < gameData.terrain[0].length; i++){
			var tile = gameData.terrain[j][i];

			gameData.terrain[j][i].content = gameData.terrain[j][i].content.filter(function(el){
				return el != null;
			});

			//income + buidings
			if(tile.owner == ownerId){
				if(tile.type == 1){
					castles.push(tile);
					strongPoints.push(tile);
					income += 100;
				} else if(tile.type == 2){
					income += 100;
				} else if(tile.type == 5){
					strongPoints.push(tile);
					income += 50;
				}
				myBuildings.push(tile);
			} else if(tile.type == 1 || tile.type == 2 || tile.type == 5){
				buildingsAround.push(tile);
			}

			//armies
			if(tile.content != null && tile.content.length > 0){
				if(tile.content[0].owner == ownerId){
					myArmies.push(tile);
					for(var a in tile.content){
						supply += tile.content[a].life / tile.content[a].maxLife;	
					}
				} else if(deadPlayers.indexOf(players[tile.content[0].owner]) == -1){
					enemiesAround.push(tile);
				}
			}
		}
	}

	console.log('Checking if all my buildings are secured...'.info);
	var insecureBuildings = [];
	for(var n in myBuildings){
		var myBuilding = myBuildings[n];
		myBuilding.secure = 0;
		for(var e in enemiesAround){
			//if enemies close
			if(distanceBetween(myBuilding, enemiesAround[e]) < 3){
				//if big threat
				if(comparePower(myBuilding, enemiesAround[e]) < 0){
					myBuilding.secure = -100;
				} else {
					myBuilding.secure = myBuilding.secure - 20;
				}
			}
		}
		if(myBuilding.secure <= securityThreshold){
			insecureBuildings.push(myBuilding);
		}
	}
	console.log(insecureBuildings.length + ' buildings unsafe !'.calcul);

	console.log('Checking if all my armies are secured...'.info);
	for(var n in myArmies){
		var myArmy = myArmies[n];
		myArmies[n].hasPlayed = false;
		myArmy.content[0].secure = 0;
		for(var e in enemiesAround){
			//if enemies close
			if(distanceBetween(myArmy, enemiesAround[e]) < 3){
				//if big threat	
				if(comparePower(myArmy, enemiesAround[e]) < 0){
					myArmy.content[0].secure = -100;
				} else {
					myArmy.content[0].secure = myArmy.content[0].secure - 20;
				}
			}
		}
		if(myArmy.content[0].secure <= securityThreshold){
			//if in a castle, give a defense order
			if(myArmy.type == 1 || myArmy == 5){
				myArmies[n].hasPlayed = true;
				for(var i in myArmy.content){
					orders.push(new Data.DefenseOrder(myArmy.y, myArmy.x, myArmy.content[i].id));
				}
			}
			//retreat to the nearest shelter 
			else { 
				var nearestShelter = null;
				for(var i in strongPoints){
					if(nearestShelter == null 
						|| distanceBetween(nearestShelter, myArmy) > distanceBetween(strongPoints[i], myArmy)){
						nearestShelter = strongPoints[i];
					}
				}
				if(nearestShelter != null){
					myArmies[n].hasPlayed = true;
					for(var i in myArmy.content){
						var destination = getNextStep(gameData.terrain, myArmy, myArmy.content[i], nearestShelter);
						if(destination != null){	
							orders.push(new Data.MoveOrder(myArmy, destination, myArmy.content[i].id));
						}
					}
				}
			}
		} else {
			//go to protect insecure place near the unit
			var nearestInsecuredPlace = null;
			for(var i in insecureBuildings){
				var distance = distanceBetween(insecureBuildings[i], myArmy);
				if(insecureBuildings[i].type == 1 && distance <= 2 || distance == 1){
					//buy unit on castle if possible
					if(insecureBuildings[i].type == 1){
						orders.push(getBuyAction(army, income - supply, gold, insecureBuildings[i]));
					}
					myArmies[n].hasPlayed = true;
					for(var i in myArmy.content){
						var destination = getNextStep(gameData.terrain, 
										myArmy, myArmy.content[i], insecureBuildings[i]);
						if(destination != null){	
							orders.push(new Data.MoveOrder(myArmy, destination, myArmy.content[i].id));
						}
					}
					break;
				}
			}

			//if unit has not played yet, attack and conquer !
			if(!myArmies[n].hasPlayed){
				var nearestPlaceToConquer = null;
				for(var i in buildingsAround){
					var distance = distanceBetween(buildingsAround[i], myArmy);
					if(comparePower(myArmy, buildingsAround[i]) > attackThreshold){
						myArmies[n].hasPlayed = true;
						for(var i in myArmy.content){
							var destination = getNextStep(gameData.terrain, 
											myArmy, myArmy.content[i], buildingsAround[i]);
							if(destination != null){
								orders.push(new Data.MoveOrder(myArmy, destination, myArmy.content[i].id));
							}
						}
						break;
					}
				}

				//if no order, defend position !
				if(!myArmies[n].hasPlayed){
					myArmies[n].hasPlayed = true;
					for(var i in myArmy.content){
						orders.push(new Data.DefenseOrder(myArmy.y, myArmy.x, myArmy.content[i].id));
					}
				}
			}
		}
	}

	//buy units with priority on insecure places
	if(income > supply * 100 && gold > 0){
		var mostInsecure = null;
		var secure = 0;
		for(var i in insecureBuildings){
			if((insecureBuildings.type == 1 || insecureBuildings.type == 5) 
				&& (secure == 0 || insecureBuildings[i].secure < secure)){
				secure = insecureBuildings[i].secure;
				mostInsecure = insecureBuildings[i];
			}
		}

		if(mostInsecure == null){
			mostInsecure = strongPoints[parseInt(strongPoints.length * Math.random())];
		}
		if(mostInsecure != null && mostInsecure.action == null && (mostInsecure.content == null || mostInsecure.content.length < 3)){
			orders.push(getBuyAction(army, income - supply, gold, mostInsecure));
		}
	}

	console.log('AI chose these orders: '.success + JSON.stringify(orders));
	return orders;
}

function getBuyAction(army, economyBalance, gold, place){
	var whichUnit = 0;
	//buy base or elite units if empty or only support units at the moment
	if(place.content == null || place.content.length == 0 || place.content[0].type % 10 == 1){
		var armyFactor = 1;
		switch (army) {
			case 1:
				armyFactor = 0.8;
			break;
			case 2:
				armyFactor = 1.3;
			break;
			case 3:
				armyFactor = 1.15;
			break;
		}
		if(economyBalance >= 100 * armyFactor && gold > 100 * armyFactor){
			whichUnit = 2;
		} else {
			whichUnit = 0;
		}
	} else {
		whichUnit = 1;
	}

	return new Data.BuyOrder(place, parseInt(whichUnit + army * 10));
}

function getNextStep(terrain, from, unit, to){
	if(to != null){
		var dx = to.x - from.x;
		var dy = to.y - from.y;
		var t = null;
		if(Math.abs(dx) > 0 && (Math.abs(dx) > Math.abs(dy) || Math.random() < 0.5)){
			t = terrain[from.y][from.x + dx/Math.abs(dx)];
		} else if(Math.abs(dy) > 0) {
			t = terrain[from.y + dy/Math.abs(dy)][from.x];
		}
		if(t != null && (unit.type != 2 && unit.type != 31 || t.type != 7)){
			return t;
		}
	}
	return null;
}

function comparePower(me, enemies){
	return getPower(me) - getPower(enemies);
}

function getPower(tile){
	var power = 0;
	if(tile.content != null){
		for(var i in tile.content){
			power += (tile.content[i].type % 10 + 1) * 100 * tile.content[i].life / tile.content[i].maxLife 
					  * tile.content[i].moral / 100;
		}
	}
	return power;
}

function distanceBetween(tile1, tile2){
	return Math.abs(tile1.x - tile2.x) + Math.abs(tile1.y - tile2.y);
}