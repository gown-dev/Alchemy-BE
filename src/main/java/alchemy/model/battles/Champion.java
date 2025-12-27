package alchemy.model.battles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import alchemy.model.pets.Pet;
import alchemy.model.pets.attributes.BaseAttribute;
import alchemy.model.pets.attributes.DerivedAttribute;
import alchemy.model.pets.moves.Move;
import alchemy.model.pets.moves.components.DamageType;
import alchemy.utils.MoveUtils;
import lombok.Getter;

@Getter
public class Champion {
	
	private final Pet pet;
	
	private int undistributed;
	
	private Map<BaseAttribute, Integer> baseAttributes;
	private Map<DerivedAttribute, Integer> derivedAttributes;
	
	private int maxHealth;
	private int health;
	
	private int energy;
	
	private int physicalShield;
	private int magicShield;
	
	private int criticalThreshold;
	private int criticalStacks;
	
	private int momentumStacks;
	private int masteryStacks;
	
	private List<Move> moves = new ArrayList<>();
	private Map<Move, Integer> cooldowns = new HashMap<>();
	
	public Champion(Pet pet) {
		this.pet = pet;
		
		baseAttributes = Stream.of(BaseAttribute.values())
				.collect(Collectors.toMap(
						(attr) -> attr, 
						(attr) -> pet.getAttributeLoadout().getBaseAttribute(attr)));
		
		derivedAttributes = Stream.of(DerivedAttribute.values())
				.collect(Collectors.toMap(
						(attr) -> attr, 
						(attr) -> pet.getAttributeLoadout().getDerivedAttribute(attr)));
		
		this.maxHealth = pet.getLevel() * 10 + pet.getAttributeLoadout().getDerivedAttribute(DerivedAttribute.TOUGHNESS) * 2;
		this.health = this.maxHealth;
		
		this.energy = pet.getLevel() + pet.getAttributeLoadout().getDerivedAttribute(DerivedAttribute.INSTINCT);
		
		this.magicShield = pet.getAttributeLoadout().getDerivedAttribute(DerivedAttribute.RESOLVE) * 2;
		
		this.moves = pet.getMoveLoadout().getMoves();
		this.cooldowns = pet.getMoveLoadout().getMoves().stream().collect(Collectors.toMap((move) -> move, (move) -> 0));
	}
	
	public boolean isAlive() {
		return health > 0;
	}
	
	public void useEnergy() {
		this.energy -= pet.getLevel();
	}
	
	public void gainEnergy() {
		this.energy += pet.getAttributeLoadout().getDerivedAttribute(DerivedAttribute.INSTINCT);
	}
	
	public void gainMomentumStacks() {
		this.momentumStacks += (pet.getAttributeLoadout().getDerivedAttribute(DerivedAttribute.MOMENTUM) + 2) / 3;
	}
	
	public void gainMasteryStacks() {
		this.masteryStacks += (pet.getAttributeLoadout().getDerivedAttribute(DerivedAttribute.MASTERY) + 2) / 3;
	}
	
	public boolean hasShield(DamageType type) {
		switch (type) {
			case MAGICAL:
				return magicShield > 0;
			case PHYSICAL:
				return physicalShield > 0;
			default:
				return false;
		}
	}
	
	public boolean isAboveCriticalThreshold() {
		return criticalStacks >= criticalThreshold;
	}
	
	public void useCriticalStacks() {
		this.criticalStacks -= this.criticalThreshold;
	}
	
	public Move getNextMove() {
		Optional<Move> nextMove = moves.stream().filter(move -> cooldowns.getOrDefault(move, 0) == 0).findFirst();
		
		nextMove.ifPresent(move -> {
			cooldowns.put(move, move.getCooldown());
		});
		
		return nextMove.orElse(MoveUtils.getDefaultMove());
	}
	
	public void applyDamages(DamageType type, int amount) {
		if (type == DamageType.MAGICAL) {
			amount = applyMagicalShieldDamages(amount);
			applyBypassDamages(amount);
		} else if (type == DamageType.PHYSICAL) {
			amount = applyPhysicalShieldDamages(amount);
			applyBypassDamages(amount);
		}
	}
		
	private int applyMagicalShieldDamages(int amount) {
		if (magicShield >= amount) {
			magicShield -= amount;
			return 0;
		} else {
			amount -= magicShield;
			magicShield = 0;
			return amount;
		}
	}
	
	private int applyPhysicalShieldDamages(int amount) {
		if (physicalShield >= amount) {
			physicalShield -= amount;
			return 0;
		} else {
			amount -= physicalShield;
			physicalShield = 0;
			return amount;
		}
	}
	
	public void applyBypassDamages(int amount) {
		if (health >= amount) {
			health -= amount;
		} else {
			amount -= health;
			health = 0;
		}
	}
	
}
