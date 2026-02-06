package alchemy.exceptions.process.pets;

import alchemy.exceptions.ProcessError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum PetProcessError implements ProcessError {

	ATTRIBUTE_INCREASE_NO_UNDISTRIBUTED_POINT("ERR_PET-F001", "No undistributed points", "When trying to increase the {} attribute, there was no available undistributed attribute point.");

	public String code;
	public String description;
	public String message;

}
