package alchemy.utils;

import java.util.Collections;

import alchemy.model.pets.moves.Move;

public class MoveUtils {
	
	public static Move getDefaultMove() {
		return Move.builder()
			.name("Splash")
			.tags(Collections.emptyList())
			.constraints(Collections.emptyList())
			.cooldown(0)
			.components(Collections.emptyList())
			.build();
	}

}
