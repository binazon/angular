package com.orange.srs.refreport.model.TO;

public class ReportingEntityPartitionTO {

	private Long entityPk;
	private String entityId;
	private String origin;
	private String type;
	private String partitionNumber;

	public ReportingEntityPartitionTO(Long entityPk, String entityId, String origin, String type,
			String partitionNumber) {
		super();
		this.entityPk = entityPk;
		this.entityId = entityId;
		this.origin = origin;
		this.type = type;
		this.partitionNumber = partitionNumber;
	}

	public Long getEntityPk() {
		return entityPk;
	}

	public void setEntityPk(Long entityPk) {
		this.entityPk = entityPk;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPartitionNumber() {
		return partitionNumber;
	}

	public void setPartitionNumber(String partitionNumber) {
		this.partitionNumber = partitionNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityPk == null) ? 0 : entityPk.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportingEntityPartitionTO other = (ReportingEntityPartitionTO) obj;
		if (entityPk == null) {
			if (other.entityPk != null)
				return false;
		} else if (!entityPk.equals(other.entityPk))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReportingEntityPartitionTO [entityPk=" + entityPk + ", entityId=" + entityId + ", origin=" + origin
				+ ", type=" + type + ", partitionNumber=" + partitionNumber + "]";
	}
}
