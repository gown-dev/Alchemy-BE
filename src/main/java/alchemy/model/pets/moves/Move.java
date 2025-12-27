package alchemy.model.pets.moves;

import java.util.List;
import java.util.stream.Collectors;

import alchemy.model.battles.Champion;
import alchemy.model.battles.events.BattleEvent;
import alchemy.model.pets.constraints.Constraint;
import alchemy.model.pets.moves.components.MoveComponent;
import alchemy.model.pets.moves.components.MoveComponentType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "Move")
public class Move {
	
	@Id
	private String name;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
	    name = "Move_Tag",
	    joinColumns = @JoinColumn(name = "move")
	)
	@Column(name = "tag")
	private List<String> tags;
	
	@OneToMany(cascade = CascadeType.ALL)
    private List<Constraint> constraints;
	
	private int cooldown;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MoveComponent> components;
	
	public List<BattleEvent> execute(Champion source, Champion target) {
		final boolean atLeastOneDamageMoveComponent = components.stream()
				.anyMatch(component -> component.getType() == MoveComponentType.DAMAGE_COMPONENT);
		final boolean isAboveCriticalThreshold = source.isAboveCriticalThreshold();
		final boolean isCriticalHit = isAboveCriticalThreshold && atLeastOneDamageMoveComponent;
		
		if (isCriticalHit) {
			source.useCriticalStacks();
		}
		
		List<BattleEvent> events = components.stream()
				.map(component -> component.execute(isCriticalHit, source, target))
				.collect(Collectors.toList());
		
		if (atLeastOneDamageMoveComponent) {
			source.gainMomentumStacks();
			source.gainMasteryStacks();
		}
		
		return events;
	}
	
}
