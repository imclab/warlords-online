module.exports = {

	GameData: function (type, terrain, lastTurn){
		this.type = type;
		this.terrain = terrain;
		this.lastTurn = lastTurn;
		if(Math.random() < 0.25){
			this.isWinter = true;	
		} else {
			this.isWinter = false;
		}
	},
	
	TerrainTile: function (type, x, y){
		this.type = type;
		this.x = x;
		this.y = y;
		this.content = [];
		this.owner = -1;
	},

	Infantry : function (owner){
		this.id = createUniqueId(owner);
		this.type = 0;
		this.owner = owner;
		this.maxLife = 1000;
		this.life = 1000;
		this.moral = 100;
		this.experience = 0;
	},
	Bowman : function (owner){
		this.id = createUniqueId(owner);
		this.type = 1;
		this.owner = owner;
		this.maxLife = 600;
		this.life = 600;
		this.moral = 100;
		this.experience = 0;
	},
	Knight: function (owner){
		this.id = createUniqueId(owner);
		this.type = 2;
		this.owner = owner;
		this.maxLife = 1000;
		this.life = 1000;
		this.moral = 100;
		this.experience = 0;
	},

	Skeleton: function (owner){
		this.id = createUniqueId(owner);
		this.type = 10;
		this.owner = owner;
		this.maxLife = 1000;
		this.life = 1000;
		this.moral = 100;
		this.experience = 0;
	},
	SkeletonBowmen: function (owner){
		this.id = createUniqueId(owner);
		this.type = 11;
		this.owner = owner;
		this.maxLife = 600;
		this.life = 600;
		this.moral = 100;
		this.experience = 0;
	},
	Necromancer: function (owner){
		this.id = createUniqueId(owner);
		this.type = 12;
		this.owner = owner;
		this.maxLife = 500;
		this.life = 500;
		this.moral = 100;
		this.experience = 0;
	},

	ChaosWarriors: function (owner){
		this.id = createUniqueId(owner);
		this.type = 20;
		this.owner = owner;
		this.maxLife = 700;
		this.life = 700;
		this.moral = 100;
		this.experience = 0;
	},
	ChaosWizards: function (owner){
		this.id = createUniqueId(owner);
		this.type = 21;
		this.owner = owner;
		this.maxLife = 500;
		this.life = 500;
		this.moral = 100;
		this.experience = 0;
	},
	Demons: function (owner){
		this.id = createUniqueId(owner);
		this.type = 22;
		this.owner = owner;
		this.maxLife = 900;
		this.life = 900;
		this.moral = 100;
		this.experience = 0;
	},

	Goblins: function (owner){
		this.id = createUniqueId(owner);
		this.type = 30;
		this.owner = owner;
		this.maxLife = 700;
		this.life = 700;
		this.moral = 100;
		this.experience = 0;
	},
	Orcs: function (owner){
		this.id = createUniqueId(owner);
		this.type = 31;
		this.owner = owner;
		this.maxLife = 900;
		this.life = 900;
		this.moral = 100;
		this.experience = 0;
	},
	Trolls: function (owner){
		this.id = createUniqueId(owner);
		this.type = 32;
		this.owner = owner;
		this.maxLife = 800;
		this.life = 800;
		this.moral = 100;
		this.experience = 0;
	},


	MoveOrder: function(from, to, unitId){
		this.type = 0;
		this.from = from.y + "," + from.x;
		this.to = to.y + "," + to.x;
		this.unitId = unitId;
	},
	DefenseOrder: function(y, x, unitId){
		this.type = 1;
		this.location = y + "," + x;
		this.unitId = unitId;
	},
	BuyOrder: function(location, unitType){
		this.type = 2;
		this.location = location.y + "," + location.x;
		this.unitType = unitType;
	}, 

	AggressiveBot: function(){
		return new Bot(-400, -400, 'aggressive');
	},
	DefensiveBot: function(){
		return new Bot(0, 0, 'defensive');
	},
	BalancedBot: function(){
		return new Bot(-100, -100, 'balanced');
	},
	RandomBot: function(){
		return new Bot(-100 * Math.random()*10, -100 * Math.random()* 10, 'random');
	}

};

function Bot(secureThreshold, attackThreshold, name){
	this.secureThreshold = secureThreshold;
	this.attackThreshold = attackThreshold;
	this.name = name;
}

var unitId = 0;
function createUniqueId(player){
	unitId += 10;
	var date = new Date();
	var components = [
	    date.getYear(),
	    date.getMonth(),
	    date.getDate(),
	    date.getHours(),
	    date.getMinutes(),
	    date.getSeconds(),
	    date.getMilliseconds()
	];
	return player + components.join('') + unitId;
}