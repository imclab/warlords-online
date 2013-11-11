var MapCreation = require("./map_creation");
var Data = require("./game_data");

module.exports = {

	createNewGameData: function (type, armies){
		return new Data.GameData(type, MapCreation.createMap(type, armies), null);
	},

	processTurn: function (gameData, actions, playersGold){
		//change the season
		if(gameData.isWinter == true && Math.random() < 0.18){
			gameData.isWinter = false;
		} else if(gameData.isWinter == false && Math.random() < 0.1){
			gameData.isWinter = true;
		}

		console.log('Actions this turn : '.info + JSON.stringify(actions));
		console.log('Gold this turn : '.info + playersGold);
		console.log('Processing actions...'.info);
		//store last turn
		gameData.lastTurn = actions;
		for(var i in actions){
			var action = actions[i];
			//process move actions
			if(action.type == 0){
				console.log('New move action from '.debug + action.from.split(',')[0] + ',' + action.from.split(',')[1]
					+ ' to '.debug + action.to.split(',')[0] + ',' + action.to.split(',')[1]);
				var units = gameData.terrain[action.from.split(',')[0]][action.from.split(',')[1]].content;
				console.log('Units on this tile '.debug + JSON.stringify(units));
				for(var n in units){
					if(units[n].id == action.unitId){
						units[n].action = action;
						for(var l = 0; l < actions.length; l++){
							if(actions[l].type == 0 && actions[l].unitId[0] != units[n].id[0]
								&& actions[l].to.split(',')[0] == units[n].action.from.split(',')[0]
								&& actions[l].to.split(',')[1] == units[n].action.from.split(',')[1]
								&& actions[l].from.split(',')[0] == units[n].action.to.split(',')[0]
								&& actions[l].from.split(',')[1] == units[n].action.to.split(',')[1]){
								console.log('Units are crossing each others !'.info);
								actions.splice(l, 1);
							}
						}
						//swamp effect
						if(gameData.terrain[action.from.split(',')[0]][action.from.split(',')[1]].type == 6 && Math.random() < 0.5){
							console.log('Stuck in the swamps !'.debug);
						} else {
							gameData.terrain[action.to.split(',')[0]][action.to.split(',')[1]].content.push(units[n]);
							gameData.terrain[action.from.split(',')[0]][action.from.split(',')[1]].content.splice(n, 1);
							console.log('Movement succeeded !'.debug);
						}
					}
				}
			} else if(action.type == 1){
				console.log('New defense action'.debug);
				var units = gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].content;
				for(var n in units){
					if(units[n].id == action.unitId){
						console.log('Defense succeeded !'.debug);
						units[n].action = action;
					}
				}
			}
		}

		for(var i in actions){
			var action = actions[i];
			if(action.type == 2 && gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].content.length < 3){
				console.log('New buy action on '.debug + action.location.split(',')[0] + ',' + action.location.split(',')[1]);
				var newUnit;
				if(action.unitType == 0){
					console.log('New Infantry !'.debug);
					newUnit = new Data.Infantry(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 50;
				} else if(action.unitType == 1){
					console.log('New Bowmen !'.debug);
					newUnit = new Data.Bowman(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 60;
				} else if(action.unitType == 2){
					console.log('New Knights !'.debug);
					newUnit = new Data.Knight(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 100;
				} else if(action.unitType == 10){
					console.log('New Skeleton !'.debug);
					newUnit = new Data.Skeleton(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 35;
				} else if(action.unitType == 11){
					console.log('New Skeleton bowmen !'.debug);
					newUnit = new Data.SkeletonBowmen(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 40;
				} else if(action.unitType == 12){
					console.log('New Necromancers !'.debug);
					newUnit = new Data.Necromancer(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 130;
				} else if(action.unitType == 20){
					console.log('New Chaos Warriors !'.debug);
					newUnit = new Data.ChaosWarriors(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 80;
				} else if(action.unitType == 21){
					console.log('New Chaos Wizards !'.debug);
					newUnit = new Data.ChaosWizards(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 120;
				} else if(action.unitType == 22){
					console.log('New Demons !'.debug);
					newUnit = new Data.Demons(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 180;
				} else if(action.unitType == 30){
					console.log('New Goblins !'.debug);
					newUnit = new Data.Goblins(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 30;
				} else if(action.unitType == 31){
					console.log('New Orcs !'.debug);
					newUnit = new Data.Orcs(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 70;
				} else if(action.unitType == 32){
					console.log('New Trolls !'.debug);
					newUnit = new Data.Trolls(gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner);
					playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] = playersGold[gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].owner] - 150;
				}
				gameData.terrain[action.location.split(',')[0]][action.location.split(',')[1]].content.push(newUnit);
			}
		}
		console.log('Gathering resources...'.info);
		var seasonFactor = 1;
		if(gameData.isWinter == true){
			seasonFactor = 0.7; 
		}
		for(var j = 0; j < gameData.terrain.length; j++){
			for(var i = 0; i < gameData.terrain[0].length; i++){
				gameData.terrain[j][i].content = gameData.terrain[j][i].content.filter(function(el){
					return el != null;
				});
				//gather resources
				if(gameData.terrain[j][i].owner >= 0 && (gameData.terrain[j][i].type == 1 
														|| gameData.terrain[j][i].type == 2)){
					console.log("+ 100 gold ".debug + " for player ".debug + gameData.terrain[j][i].owner);
					playersGold[gameData.terrain[j][i].owner] = parseInt(playersGold[gameData.terrain[j][i].owner]) + 100 * seasonFactor;
				} else if(gameData.terrain[j][i].owner >= 0 && gameData.terrain[j][i].type == 5){
					console.log("+ 50 gold ".debug + " for player ".debug + gameData.terrain[j][i].owner);
					playersGold[gameData.terrain[j][i].owner] = parseInt(playersGold[gameData.terrain[j][i].owner]) + 50 * seasonFactor;
				}

				//supply units
				for(n in gameData.terrain[j][i].content){
					var unit = gameData.terrain[j][i].content[n];
					if(unit != null){
						if(unit.life <= 0){
							gameData.terrain[j][i].content.splice(n, 1);
						} else{
							if(unit.type == 0){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 50*unit.life/unit.maxLife);
							} else if(unit.type == 1){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 60*unit.life/unit.maxLife);
							} else if(unit.type == 2){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 100*unit.life/unit.maxLife);
							} else if(unit.type == 10){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 35*unit.life/unit.maxLife);
							} else if(unit.type == 11){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 40*unit.life/unit.maxLife);
							} else if(unit.type == 12){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 130*unit.life/unit.maxLife);
							} else if(unit.type == 20){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 80*unit.life/unit.maxLife);
							} else if(unit.type == 21){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 120*unit.life/unit.maxLife);
							} else if(unit.type == 22){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 180*unit.life/unit.maxLife);
							} else if(unit.type == 30 && gameData.terrain[j][i].type != 7 && gameData.terrain[j][i].type != 3){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 30*unit.life/unit.maxLife);
							} else if(unit.type == 31){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 70*unit.life/unit.maxLife);
							} else if(unit.type == 32){
								playersGold[unit.owner] = parseInt(playersGold[unit.owner] - 150*unit.life/unit.maxLife);
							}
						}
					}
				}
			}
		}
		console.log("Players Gold : ".calcul + playersGold);
		console.log('Processing battles...'.info);
		//battles + conquer
		var playersData = [];
		for(var j = 0; j < gameData.terrain.length; j++){
			for(var i = 0; i < gameData.terrain[0].length; i++){
				if(gameData.terrain[j][i].content.length > 0){
					var isBattle = false;
					for(n in gameData.terrain[j][i].content){
						var unit = gameData.terrain[j][i].content[n];
						if(unit != null){
							//check supply
							if(playersGold[unit.owner] < 0){
								console.log('A unit is starving...'.info);
								unit.moral += playersGold[unit.owner]/4;
								if(unit.moral < 40){
									console.log('A unit is deserting...'.info);
									unit.life += 7 * (unit.moral - 50);
									if(unit.life <= 0){
										gameData.terrain[j][i].content.splice(n, 1);
									}
									if(unit.moral < 0){
										unit.moral = 0;
									}
								}
							}
							if(gameData.terrain[j][i].content.length > 0 
								&& gameData.terrain[j][i].content[0].owner != unit.owner){
								//battle !
								isBattle = true;
								}
						}
					}

					if(gameData.terrain[j][i].content.length == 0){
						continue;
					}
					if(isBattle){
						console.log('Battle on tile '.info + i + ',' + j);
						processBattle(gameData, i, j);
					} else if(gameData.terrain[j][i].type == 1 || gameData.terrain[j][i].type == 5){
						for(var n in gameData.terrain[j][i].content){
							if(gameData.terrain[j][i].content[n] != null){
								gameData.terrain[j][i].content[n].life = Math.min(gameData.terrain[j][i].content[n].maxLife, gameData.terrain[j][i].content[n].life + 200);
								gameData.terrain[j][i].content[n].moral = Math.min(100, gameData.terrain[j][i].content[n].moral + 25);
							}
						}
					} else if(gameData.terrain[j][i].type == 2){
						for(var n in gameData.terrain[j][i].content){
							if(gameData.terrain[j][i].content[n] != null){
								gameData.terrain[j][i].content[n].moral = Math.min(100, gameData.terrain[j][i].content[n].moral + 25);
							}
						}
					} else if(gameData.terrain[j][i].type == 6){
						for(var n in gameData.terrain[j][i].content){
							if(gameData.terrain[j][i].content[n] != null){
								gameData.terrain[j][i].content[n].moral = Math.max(0, gameData.terrain[j][i].content[n].moral - 20);
								gameData.terrain[j][i].content[n].experience = Math.min(100, gameData.terrain[j][i].content[n].experience + 10);
							}
						}
					} else{
						for(var n in gameData.terrain[j][i].content){
							if(gameData.terrain[j][i].content[n] != null){
								gameData.terrain[j][i].content[n].moral = Math.min(100, gameData.terrain[j][i].content[n].moral + 15);
							}
						}
					}
					//demons-trolls regeneration
					for(var n in gameData.terrain[j][i].content){
						if(gameData.terrain[j][i].content[n].type == 22 || gameData.terrain[j][i].content[n].type == 32){
							gameData.terrain[j][i].content[n].life = Math.min(gameData.terrain[j][i].content[n].maxLife, gameData.terrain[j][i].content[n].life + 150);
						}
					}
				}
			}
		}
		//adjusting number of armies on tiles
		for(var j = 0; j < gameData.terrain.length; j++){
			for(var i = 0; i < gameData.terrain[0].length; i++){
				if(gameData.terrain[j][i].content.length > 3){
					console.log('Need to equalize on '.info + j + ',' + i + ' !'.info);
					for(var n in gameData.terrain[j][i].content){
						if(gameData.terrain[j][i].content[n].action != null
							&& gameData.terrain[j][i].content[n].action.type == 0
							&& canMoveThere(gameData.terrain[j][i].content[n], gameData.terrain[gameData.terrain[j][i].content[n].action.from.split(',')[0]][gameData.terrain[j][i].content[n].action.from.split(',')[1]])){
							gameData.terrain[gameData.terrain[j][i].content[n].action.from.split(',')[0]][gameData.terrain[j][i].content[n].action.from.split(',')[1]].content.push(gameData.terrain[j][i].content[n]);
							gameData.terrain[j][i].content.splice(n, 1);
							console.log('Movement back to equalize !'.info);
						}
					}
					if(gameData.terrain[j][i].content.length > 3){
						console.log('Need to force equalization'.info);
						var unitEjected = [];
						unitEjected.push(gameData.terrain[j][i].content[parseInt(gameData.terrain[j][i].content.length * Math.random())]);
						moveRoutedArmy(gameData, unitEjected, gameData.terrain[j][i]);
					}
				}
			}
		}

		//conquer places	
		for(var j = 0; j < gameData.terrain.length; j++){
			for(var i = 0; i < gameData.terrain[0].length; i++){
				if(gameData.terrain[j][i].content.length > 0){
					if(gameData.terrain[j][i].type == 1 || gameData.terrain[j][i].type == 2
						|| gameData.terrain[j][i].type == 5){
						if(gameData.terrain[j][i].content[0] != null){
							gameData.terrain[j][i].owner = gameData.terrain[j][i].content[0].owner;
						}
					}
					for(n in gameData.terrain[j][i].content){
						if(gameData.terrain[j][i].content[n] != null){
							gameData.terrain[j][i].content[n].action = undefined;
						}
					}
				}
			}
		}

		gameData.playersGold = playersGold;
		return gameData;
	},

	getDeadPlayers: function (players, gameData){
		console.log('Checking if some players have lost the game...'.debug);
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
					if(deadPlayers.length == 0){
						return [];
					}
				}
			}
		}
		return deadPlayers;
	}

};

function processBattle(gameData, x, y){
	console.log('Ordering armies in battle '.debug + x + ',' + y);
	var tile = gameData.terrain[y][x];
	var attackerComeFrom = [];
	var battle = reorderArmies(tile);
	console.log('Armies ready for battle'.info);
	console.log(battle);
	console.log('Contact units are moving...'.debug);
	battle = bowmenShoot(tile, battle);
	console.log(battle);
	var n = 0;
	while(checkIfBattleEnd(battle) == false){
		n++;
		console.log('*** Round '.calcul + n + ' ***'.calcul);
		battle = contactFight(tile, battle, n);
		console.log(battle);
	}
	console.log('Ending battle after '.calcul + n + ' rounds'.calcul);
	console.log('Add experience'.debug);
	gameData.terrain[y][x].content = gameData.terrain[y][x].content.filter(function(el){
		return el != null;
	});
	gameData.terrain[y][x].content = gameData.terrain[y][x].content.filter(function(el){
		return el.life > 0;
	});
	for(var i in tile.content){
		gameData.terrain[y][x].content[i].experience = Math.min(100, tile.content[i].experience + n * 5);
	}
	var averageMoral = [];
	for(var i in battle){
		if(battle[i].length > 0){
			averageMoral[i] = 0;
			for(var j in battle[i]){
				averageMoral[i] = averageMoral[i] + battle[i][j].moral;
			}
			if(averageMoral[i] / battle[i].length < 45){
				console.log('Moving routed army... morale ='.calcul + averageMoral[i] / battle[i].length);
				moveRoutedArmy(gameData, battle[i], tile);
			}
		}
	}
	console.log('Add morale to winners'.debug);
	for(var i in gameData.terrain[y][x].content){
		gameData.terrain[y][x].content[i].moral = Math.min(100, gameData.terrain[y][x].content[i].moral + 15);
	}
}

function canMoveThere(unit, tile){
	if(tile.content != null && (tile.content.length >= 3 || tile.content.length > 0 && tile.content[0].owner != unit.owner)){
		return false;
	} else if((unit.type == 2 || unit.type == 31) && tile.type == 7){
		return false;
	} else {
		return true;
	}
}

function moveRoutedArmy(gameData, army, tile){
	for(var n in army){
		//attacker are going back where they come if possible
		if(army[n].action != null && army[n].action.type == 0){
			for(var i in tile.content){
				if(tile.content[i].id == army[n].id){
					tile.content.splice(i, 1);
					if(canMoveThere(army[n], gameData.terrain[army[n].action.from.split(',')[0]][army[n].action.from.split(',')[1]])){
						gameData.terrain[army[n].action.from.split(',')[0]][army[n].action.from.split(',')[1]].content.push(army[n]);
					}
					break;
				}
			}
		}
		//defenders flee where they can 
		else {
			var possibleTiles = [];
			if(tile.x - 1 >= 0 && canMoveThere(army[n], gameData.terrain[tile.y][tile.x - 1])){
				possibleTiles.push(gameData.terrain[tile.y][tile.x - 1]);
			}
			if(tile.x + 1 < gameData.terrain[0].length && canMoveThere(army[n], gameData.terrain[tile.y][tile.x + 1])){
				possibleTiles.push(gameData.terrain[tile.y][tile.x + 1]);
			}
			if(tile.y - 1 >= 0 && canMoveThere(army[n], gameData.terrain[tile.y - 1][tile.x])){
				possibleTiles.push(gameData.terrain[tile.y - 1][tile.x]);
			}
			if(tile.y + 1 < gameData.terrain.length && canMoveThere(army[n], gameData.terrain[tile.y + 1][tile.x])){
				possibleTiles.push(gameData.terrain[tile.y + 1][tile.x]);
			}
			//remove attacker's initial position
			console.log('Checking possible tiles...'.debug);
			for(var m in possibleTiles){
				for(var i in tile.content){
					if(tile.content[i].owner != army[0].owner && tile.content[i].action != null 
						&& tile.content[i].action.type == 0 && possibleTiles[m] != null
						&& tile.content[i].action.from == possibleTiles[m].y + ',' + possibleTiles[m].x){
						possibleTiles.splice(m, 1);
						break;
					}
				}	
			}
			for(var i in tile.content){
				if(tile.content[i].id == army[n].id){
					tile.content.splice(i, 1);
					if(possibleTiles.length > 0){
						var destinationTile = possibleTiles[parseInt(Math.random() * possibleTiles.length)];
						gameData.terrain[destinationTile.y][destinationTile.x].content.push(army[n]);
					}
					break;
				}
			}
		}
	}
}

function checkIfBattleEnd(battle){
	var nbArmies = 0;
	var averageMoral = [];
	for(var i in battle){
		if(battle[i].length > 0){
			nbArmies++;
			averageMoral[i] = 0;
			for(var j in battle[i]){
				averageMoral[i] = averageMoral[i] + battle[i][j].moral;
			}
			if(averageMoral[i] / battle[i].length < 35){
				nbArmies--;
				console.log('One army is routed'.debug);
			}
		}
	}
	if(nbArmies < 2){
		return true;
	}else{
		return false;
	}
}

function reorderArmies(tile){
	console.log('Reordering armies...'.debug);
	tile.content = tile.content.filter(function(el){
		return el.life > 0;
	});

	var battle = [];
	for(var i in tile.content){
		if(tile.content[i].life > 0){
			if(i == 0){
				battle.push(new Array());
				battle[0].push(tile.content[i]);
			} else{
				for(var n in battle){
					if(battle[n].length > 0 && battle[n][0].owner == tile.content[i].owner){
						battle[n].push(tile.content[i]);
						break;
					}
					if(n == battle.length - 1){
						battle.push(new Array());
						battle[battle.length - 1].push(tile.content[i]);
					}
				}
			}
		}
	}
	return battle;
}

function bowmenShoot(tile, battle){
	var damages = new Array();
	for(var i in battle){
		damages[i] = [];
		for(var j in battle[i]){
			damages[i][j] = 0;
		}
	}
	for(var i in battle){
		for(var j in battle[i]){
			if(battle[i][j].type == 1 || battle[i][j].type == 11){
				var n = i;
				while(n == i){
					n = parseInt(Math.random()*battle.length);
				}
				console.log('Bowmen are shooting arrows...'.debug);
				if(battle[n] != null){
					var rang = parseInt(Math.random()*battle[n].length);
					damages[n][rang] = damages[n][rang] + fight(tile, battle[i][j], battle[n][rang], false);
				}
			}
		}
	}
	for(var i in damages){
		for(var j in damages[i]){
			if(damages[i][j] > 0){
				battle[i][j].life = battle[i][j].life - damages[i][j];
				if(battle[i][j].type < 20 || battle[i][j].type >= 30){
					battle[i][j].moral = Math.max(0, parseInt(battle[i][j].moral - damages[i][j]/10));
				}
			}
		}
	}
	return reorderArmies(tile, battle);
}

function contactFight(tile, battle, round){
	var damages = new Array();
	for(var i in battle){
		damages[i] = [];
		for(var j in battle[i]){
			damages[i][j] = 0;
		}
	}
	for(var i in battle){
		for(var j in battle[i]){
			var n = i;
			while(n == i){
				n = parseInt(Math.random()*battle.length);
			}
			console.log('Contact units are fighting...'.debug);
			console.log(battle[i][j]);
			if(battle[n] != null){
				var rang = parseInt(Math.random()*battle[n].length);
				damages[n][rang] = damages[n][rang] + fight(tile, battle[i][j], battle[n][rang], true);
			}
		}
	}
	//special effects
	for(var i in damages){
		for(var j in damages[i]){
			//cancel first round damages to allies
			if(round == 1 && battle[i][j].type == 21){
				for(var n in damages[i]){
					damages[i][n] = 0;
				}
				break;
			}
		}
	}
	for(var i in damages){
		for(var j in damages[i]){
			if(damages[i][j] > 0){
				battle[i][j].life = battle[i][j].life - damages[i][j];
				if(battle[i][j].type < 20 || battle[i][j].type >= 30){
					battle[i][j].moral = Math.max(0, parseInt(battle[i][j].moral - damages[i][j]/10));
				}
			}
		}
	}
	return reorderArmies(tile, battle);
}

function fight(tile, attacker, target, isContact){
	var damage = Math.max(0, parseInt(attacker.life * (attacker.moral/100 + attacker.experience/100) * (0.3*Math.random()) * fightModifiers[attacker.type][target.type]));
	//attack in circle
	if(attacker.action != null && attacker.action.type == 0){
		for(var i in tile.content){
			if(tile.content[i].owner == attacker.owner && tile.content[i].action != null 
				&& tile.content[i].action.type == 0
				&& tile.content[i].action.from != attacker.action.from){
				damage *= 1.15;
				break;
			}
		}
	}
	//defense order
	if(target.action != null && target.action.type == 1){
		//no orcs
		if(target.type >= 30 && target.type < 40){
		} else {
			damage *= 0.8;
		}
	}
	
	//bowmen attack
	if(attacker.type == 1 || attacker.type == 11){
		if(isContact){
			//in close combat
			damage *= 0.5;
			for(var i in tile.content){
				if(tile.content[i].owner == attacker.owner && tile.content[i].type != 1 && tile.content[i].type != 11){
					//if some contact units protect them
					damage /= 0.5;
					break;
				}
			}
		} else {
			if(attacker.action != null && attacker.action.type == 1){
				//when defending position
				damage *= 1.2;
			}
		}

		//if in forest
		if(tile.type == 3){
			damage *= 0.8;
		}
	}

	//bonus terrains in defense
	if((tile.type == 1 || tile.type == 5) && (target.action == null || target.action.type == 1)){
		damage *= 0.5;
	}



	//undead
	if(attacker.type >= 10 && attacker.type <= 12){
		console.log(parseInt(0.15 * damage) + ' undead resurrect'.debug);
		attacker.life = Math.min(attacker.maxLife, attacker.life + 0.15 * damage);
		for(var i in tile.content){
			if(tile.content[i].type == 12 && tile.content[i].owner == attacker.owner){
				console.log(parseInt(0.15 * damage) + ' undead resurrect due to necromancers !'.debug);
				attacker.life = Math.min(attacker.maxLife, attacker.life + parseInt(0.15 * damage));
				break;
			} 
		}
		for(var i in tile.content){
			if(tile.content[i].id == target.id){
				tile.content[i].moral = Math.max(0, target.moral - 3);
			}
		}
	}

	//orcs & goblins
	if(attacker.type >= 30 && attacker.type < 40){
		if(attacker.action != null && attacker.action.type == 0){
			damage *= 1.2;
		}
	}

	//critical strike
	if(attacker.type == 22 && Math.random > 0.8){
		damage *= 1.5;
	}

	console.log('Damage = '.calcul + damage);
	return damage;
}

var fightModifiers = [
	[1, 1.2, 0.8, 0, 0, 0, 0, 0, 0, 0, 1.2, 1.3, 0.8, 0, 0, 0, 0, 0, 0, 0, 0.6, 0.8, 0.8, 0, 0, 0, 0, 0, 0, 0, 1.2, 0.9, 0.6],
	[1, 1, 0.8, 0, 0, 0, 0, 0, 0, 0, 0.8, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0.5, 0.8, 0.7, 0, 0, 0, 0, 0, 0, 0, 1.3, 0.9, 0.8],
	[1.3, 1.5, 1, 0, 0, 0, 0, 0, 0, 0, 1.5, 1.7, 0.8, 0, 0, 0, 0, 0, 0, 0, 0.8, 1, 0.8, 0, 0, 0, 0, 0, 0, 0, 1.5, 1.2, 1],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0.6, 0.8, 0.5, 0, 0, 0, 0, 0, 0, 0, 1, 1.2, 0.8, 0, 0, 0, 0, 0, 0, 0, 0.4, 0.6, 0.6, 0, 0, 0, 0, 0, 0, 0, 1.1, 0.8, 0.6],
	[1, 1, 0.8, 0, 0, 0, 0, 0, 0, 0, 0.8, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0.3, 0.5, 0.5, 0, 0, 0, 0, 0, 0, 0, 1.2, 0.9, 0.8],
	[1.2, 1.2, 1.2, 0, 0, 0, 0, 0, 0, 0, 1.2, 1.2, 1, 0, 0, 0, 0, 0, 0, 0, 0.8, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1.6, 1, 1],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[1.6, 1.7, 1.3, 0, 0, 0, 0, 0, 0, 0, 1.8, 1.9, 1.4, 0, 0, 0, 0, 0, 0, 0, 1, 1.2, 0.8, 0, 0, 0, 0, 0, 0, 0, 1.7, 1.3, 1.2],
	[1.4, 1.5, 1.2, 0, 0, 0, 0, 0, 0, 0, 1.6, 1.7, 1, 0, 0, 0, 0, 0, 0, 0, 1.2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1.7, 1.2, 1.1],
	[1.5, 1.4, 1.2, 0, 0, 0, 0, 0, 0, 0, 1.4, 1.6, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 2, 1.3, 1],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
	[0.8, 1, 0.6, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0.8, 0, 0, 0, 0, 0, 0, 0, 0.6, 0.5, 0.4, 0, 0, 0, 0, 0, 0, 0, 1, 0.7, 0.6],
	[1, 1, 0.8, 0, 0, 0, 0, 0, 0, 0, 1.2, 1.2, 1, 0, 0, 0, 0, 0, 0, 0, 0.8, 0.9, 0.8, 0, 0, 0, 0, 0, 0, 0, 1.2, 1, 0.8],
	[1.2, 1.2, 1.3, 0, 0, 0, 0, 0, 0, 0, 1.1, 1.2, 1.3, 0, 0, 0, 0, 0, 0, 0, 1.2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1.2, 1.4, 1]
	];