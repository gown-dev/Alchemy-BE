package alchemy.model.pets.attributes;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import alchemy.exceptions.ProcessException;
import alchemy.exceptions.process.pets.PetProcessError;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AttributeLoadout")
public class AttributeLoadout {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	private int undistributed;

	private int strength;
	private int constitution;
	private int agility;
	private int intellect;
	private int willpower;

	@Transient
	public int getBaseAttribute(BaseAttribute attribute) {
		switch (attribute) {
			case STRENGTH:
				return strength;
			case CONSTITUTION:
				return constitution;
			case AGILITY:
				return agility;
			case INTELLECT:
				return intellect;
			case WIILPOWER:
				return willpower;
		}

		return 0;
	}

	@Transient
	public int getDerivedAttribute(DerivedAttribute attribute) {
		return getBaseAttribute(attribute.getBaseAttribute1()) + getBaseAttribute(attribute.getBaseAttribute2());
	}

	@Transient
	public void increaseBaseAttribute(BaseAttribute attribute) {
		if (undistributed <= 0) {
			throw new ProcessException(PetProcessError.ATTRIBUTE_INCREASE_NO_UNDISTRIBUTED_POINT, HttpStatus.BAD_REQUEST, attribute.toString());
		}

		switch (attribute) {
			case STRENGTH:
				this.strength = strength + 1;
			case CONSTITUTION:
				this.constitution = constitution + 1;
			case AGILITY:
				this.agility = agility + 1;
			case INTELLECT:
				this.intellect = intellect + 1;
			case WIILPOWER:
				this.willpower = willpower + 1;
		}
	}

}
