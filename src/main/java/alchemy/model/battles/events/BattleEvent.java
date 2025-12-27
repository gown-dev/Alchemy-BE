package alchemy.model.battles.events;

public record BattleEvent (
		BattleEventType type,
		String message
		){ }
