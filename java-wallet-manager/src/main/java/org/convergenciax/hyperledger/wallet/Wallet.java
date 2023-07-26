package org.convergenciax.hyperledger.wallet;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

/*
 *Copyright ConvergenciaX Spa.
 * Autor: Joel Cotrado Vargas <joel.cotrado@gmail.com>
 * Date: 01-12-2022
 *
 */


@DataType()
public final class Wallet {
	
	@Property()
	private Double tokenAmout;
	
	@Property()
	private String walletId;
	
	@Property()
	private String ownerId;


	public Wallet(@JsonProperty("tokenAmount") final Double tokenAmout, @JsonProperty("walletId") final String walletId) {
		super();
		this.tokenAmout = tokenAmout;
		this.walletId = walletId;
	}


	public Double getTokenAmout() {
		return tokenAmout;
	}

	/*
	 * public void setTokenAmout(Double tokenAmout) { this.tokenAmout = tokenAmout;
	 * }
	 */

	public String getWalletId() {
		return walletId;
	}


	public void setWalletId(String walletId) {
		this.walletId = walletId;
	}
 
	@Override
	public boolean equals(final Object obj) {
		if (this == obj )
			return true;
		
		if ( (obj== null) || (getClass() != obj.getClass()))
			return false;
		
		Wallet other = (Wallet) obj;
		
		
		return Objects.deepEquals(new String[] { getTokenAmout().toString(), getWalletId() },
									new String[] { other.getTokenAmout().toString(), other.getWalletId() });
	}
 

	@Override
	public int hashCode() {
		return Objects.hash(this.getTokenAmout(),this.getWalletId());
	}
	
	@Override
	public String toString() {
		
		return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode())+ " [tokenAmount :" + this.tokenAmout +" - WalletId :" + this.walletId +"] ";
		
	}
	
}
