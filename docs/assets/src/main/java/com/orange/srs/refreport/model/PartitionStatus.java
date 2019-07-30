package com.orange.srs.refreport.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.orange.srs.refreport.technical.Configuration;

@Entity
@Table(name = PartitionStatus.TABLE_NAME)
public class PartitionStatus {

	public static final String TABLE_NAME = "T_PARTITION_STATUS";

	public static final String FIELD_DATE = "date";
	public static final String FIELD_PARTITION_NUMBER = "partitionNumber";
	public static final String FIELD_NUMBER_OF_ENTITY = "numberOfEntity";

	@Id
	@Column(name = "DATE", length = 6)
	public String date;

	@Column(name = "PARTITION_NUMBER")
	public int partitionNumber;

	@Column(name = "NUMBER_OF_ENTITY")
	public int numberOfEntity;

	public PartitionStatus() {
		this.date = "";
		this.partitionNumber = 1;
		this.numberOfEntity = 0;
	}

	public PartitionStatus(String date, int partitionNumber, int numberOfEntity) {
		this.date = date;
		this.partitionNumber = partitionNumber;
		this.numberOfEntity = numberOfEntity;
	}

	public void incrementNumberOfEntity() {
		if (this.numberOfEntity >= Configuration.partitionSize) {
			incrementPartitionNumber();
		}
		this.numberOfEntity++;
	}

	public void incrementPartitionNumber() {
		this.partitionNumber++;
		this.numberOfEntity = 0;
	}

	@Override
	public String toString() {
		return "PartitionStatus [date=" + date + ", partitionNumber=" + partitionNumber + ", numberOfEntity="
				+ numberOfEntity + "]";
	}
}
