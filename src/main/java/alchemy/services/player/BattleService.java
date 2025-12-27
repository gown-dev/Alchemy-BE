package alchemy.services.player;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import alchemy.model.battles.Champion;
import alchemy.model.battles.events.BattleEvent;
import alchemy.model.battles.events.BattleEventType;
import alchemy.model.pets.Pet;
import alchemy.model.pets.moves.Move;

public class BattleService {
	
	public void executeBattle(Pet pet1, Pet pet2) {
		Champion champion1 = new Champion(pet1);
		Champion champion2 = new Champion(pet2);
		
		List<BattleEvent> events = new ArrayList<>();
		
		Queue<Champion> turns = new PriorityQueue<>((c1, c2) -> Integer.compare(
				champion1.getEnergy(), 
				champion2.getEnergy()));
		
		while(champion1.isAlive() && champion2.isAlive()) {	
			champion1.gainEnergy();
			champion2.gainEnergy();		
			
			Champion active = turns.peek();
			Champion opponent = (active == champion1) ? champion2 : champion1;
			
			events.addAll(playTurn(active, opponent));
		}
		
		if (!champion1.isAlive()) {
			events.add(new BattleEvent(BattleEventType.CHAMPION_FAINTED, champion1.getPet().getName() + " fainted !"));
		}
		
		if (!champion2.isAlive()) {
			events.add(new BattleEvent(BattleEventType.CHAMPION_FAINTED, champion2.getPet().getName() + " fainted !"));
		}
		
		if (!champion1.isAlive() && !champion2.isAlive()) {
			events.add(new BattleEvent(BattleEventType.BATTLE_ENDED, "It's a draw !"));
		} else if (!champion2.isAlive()) {
			events.add(new BattleEvent(BattleEventType.BATTLE_ENDED, champion1.getPet().getName() + " won !"));
		} else if (!champion1.isAlive()) {
			events.add(new BattleEvent(BattleEventType.BATTLE_ENDED, champion2.getPet().getName() + " won !"));
		}
	}
	
	private List<BattleEvent> playTurn(Champion player, Champion opponent) {
		player.useEnergy();
		
		Move move = player.getNextMove();
		return move.execute(player, opponent);
	}

}
