package alchemy.model.pets.moves.components;

import java.util.UUID;

import alchemy.model.battles.Champion;
import alchemy.model.battles.events.BattleEvent;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorColumn(name = "move_component_type")
@Table(name = "MoveComponents")
public abstract class MoveComponent {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	public abstract MoveComponentType getType();
	public abstract BattleEvent execute(boolean criticalHit, Champion source, Champion target);
	
}
