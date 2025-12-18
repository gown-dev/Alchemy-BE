package alchemy.exceptions.process.admin;

import alchemy.exceptions.ProcessError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum AdminProcessError implements ProcessError {

	GENE_CREATION_ALREADY_EXIST("ERR_ADM-F001", "Duplicates exist", "Upon gene creation, the following keys already exist : {}"),
	GENE_DELETION_DOES_NOT_EXIST("ERR_ADM-F002", "Gene does not exist", "Upon gene deletion, the following keys couldn't be found and deleted : {}");

	public String code;
	public String description;
	public String message;
	
}
