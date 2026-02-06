package alchemy.model.pets.moves.components;

import alchemy.model.battles.Champion;
import alchemy.model.battles.events.BattleEvent;
import alchemy.model.battles.events.BattleEventType;
import alchemy.model.pets.attributes.DerivedAttribute;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("DAMAGE_COMPONENT")
public class DamageComponent extends MoveComponent {

	private int baseDamage;
	private int baseBypass;
	private DamageType damageType;

	@Override
	public MoveComponentType getType() {
		return MoveComponentType.DAMAGE_COMPONENT;
	}

	@SuppressWarnings("unused")
	@Override
	public BattleEvent execute(boolean isCriticalHit, Champion source, Champion target) {
		StringBuilder result = new StringBuilder();

		int baseAmount = baseDamage;
		int basePiercing = baseBypass;

		int criticalAmount = 0;
		int criticalBypass = 0;

		if (isCriticalHit) {
			criticalAmount = (baseAmount + 1) / 2;
			criticalBypass = (basePiercing + 1) / 2;
		}

		int momentumDamages = 0;
		int masteryDamages = 0;
		int clarityBypass = 0;
		int clarityDoubled = 0;

		if (damageType == DamageType.PHYSICAL) {
			momentumDamages = source.getMomentumStacks();
		} else if (damageType == DamageType.MAGICAL) {
			masteryDamages = source.getMasteryStacks();
			clarityBypass = source.getDerivedAttributes().getOrDefault(DerivedAttribute.CLARITY, 0);

			if (target.hasShield(damageType)) {
				clarityDoubled = clarityBypass;
			}
		}

		baseDamage = baseAmount + criticalAmount + momentumDamages + masteryDamages;
		baseBypass = basePiercing + criticalBypass + clarityBypass + clarityDoubled;

		int reduction = 0;

		int defenceReduction = 0;
		int adaptationReduction = 0;

		if (damageType == DamageType.PHYSICAL) {
			defenceReduction = source.getDerivedAttributes().getOrDefault(DerivedAttribute.DEFENCE, 0);
		} else if (damageType == DamageType.MAGICAL) {
			adaptationReduction = source.getDerivedAttributes().getOrDefault(DerivedAttribute.ADAPTATION, 0);
		}

		reduction = defenceReduction + adaptationReduction;

		int amountReduction = 0;
		int bypassReduction = 0;
		int overReduction = 0;

		result.append(baseDamage);

		if (damageType == DamageType.MAGICAL) {
			result.append(" magical damages (");
		} else if (damageType == DamageType.PHYSICAL) {
			result.append(" physical damages (");
		}

		result.append(baseAmount).append(" (Base)");

		if (criticalAmount > 0) {
			result.append("+ ").append(criticalAmount).append(" (Critical Hit)");
		}

		if (momentumDamages > 0) {
			result.append("+ ").append(momentumDamages).append(" (Momentum)");
		}

		if (masteryDamages > 0) {
			result.append("+ ").append(masteryDamages).append(" (Mastery)");
		}

		result.append(")");

		if (baseBypass > 0) {
			result.append(" + ").append(baseBypass);

			if (damageType == DamageType.MAGICAL) {
				result.append(" magical");
			} else if (damageType == DamageType.PHYSICAL) {
				result.append(" physical");
			}

			result.append(" piercing damages (");

			if (basePiercing > 0) {
				result.append(basePiercing).append(" (Base)");
			}

			if (criticalBypass > 0) {
				result.append(" + ").append(criticalBypass).append(" (Critical Hit)");
			}

			if (clarityBypass > 0) {
				result.append(" + ").append(clarityBypass).append(" (Clarity)");
			}

			if (clarityDoubled > 0) {
				result.append(" + ").append(clarityDoubled).append(" (No Magic Shield)");
			}
		}

		result.append(").");

		if (reduction > 0) {
			result.append(" The damages were reduced by ").append(reduction).append(" (");

			if (reduction <= baseDamage) {
				amountReduction = reduction;
				baseDamage -= reduction;
			} else {
				amountReduction = baseDamage;
				reduction -= baseDamage;
				baseDamage = 0;
				if (reduction <= baseBypass) {
					bypassReduction = reduction;
					baseBypass -= reduction;
				} else {
					bypassReduction = baseBypass;
					reduction -= baseBypass;
					baseBypass = 0;
					overReduction = reduction;
				}
			}

			if (adaptationReduction > 0) {
				result.append(criticalAmount).append(" (Adaptation)");
			} else if (defenceReduction > 0) {
				result.append(defenceReduction).append(" (Defense)");
			}

			result.append("), for a total of ").append(baseBypass + baseDamage).append(" damages dealt.");
		}

		target.applyBypassDamages(baseBypass);
		target.applyDamages(damageType, baseDamage);

		return new BattleEvent(BattleEventType.DAMAGE_MOVE, result.toString());
	}

}
