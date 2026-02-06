package alchemy.exceptions.confirmation.admin;

import alchemy.exceptions.ConfirmationError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum AdminConfirmationError implements ConfirmationError {

	GENES_SYNCHRONIZATION_CONFIRM("CONF_ADM-C001", "Genes Synchronization", "Requested synchronization causes the deletion of the following genes : {} ; Please confirm by using the confirmation header.", "x-confirm-sync-genes");

	public String code;
	public String description;
	public String message;
	public String confirmationKey;

}
