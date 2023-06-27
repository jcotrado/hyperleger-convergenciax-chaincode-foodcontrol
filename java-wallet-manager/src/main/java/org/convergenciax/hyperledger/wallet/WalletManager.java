package org.convergenciax.hyperledger.wallet;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

import com.owlike.genson.Genson;

/*
 *Copyright ConvergenciaX Spa.
 * Autor: Joel Cotrado Vargas <joel.cotrado@gmail.com>
 * Date: 01-12-2022
 * 
 * stub interactua con el libro mayor, permite recibir el nombre de la funcion y parametros de entrada.
 */

@Contract(	name = "WalletManager", 
			info = @Info(title = "WalletManager Contract - ConvergenciaX", description = "WalletManager Contract base - ConvergenciaX", version = "0.0.1", license = @License(name = "ConvergenciaX Spa"), 
			contact = @Contact(email = "joel.cotrado@gmail.com", 
			name = "Joel Cotrado Vargas", url = "")))

@Default
public final class WalletManager implements ContractInterface {

	private final Genson genson = new Genson();

	private enum WalletManagerErrors {
		WALLET_NOT_FOUND, WALLET_ALREADY_EXISTS, AMOUNTFORMAT_ERROR, TOKENAMOUNTNOTENOUGH
	}

	/**
	 * 
	 * @param ctx
	 */
	@Transaction
	public void initLedger(final Context ctx) {
	}

	/**
	 * 
	 * @param ctx
	 * @param walletId
	 * @param tokenAmountStr
	 * @param owner
	 * @return
	 */
	@Transaction
	public Wallet createWallet(final Context ctx, final String walletId, final String tokenAmountStr,
			final String owner) {
		// Implementar todas las validaciones necesarias, cualquier nodo aprobador puede
		// recibir la transacción.
		// validar nulos, formatos, etc.
		//

		// verifica que el monto inicial sea valido
		double tokenAmountInicial = 0.0;
		try {
			tokenAmountInicial = Double.parseDouble(tokenAmountStr);
			if (tokenAmountInicial < 0.0) {
				String errorMensaje = String.format("Monto [%s ] incorrecto", tokenAmountStr);
				throw new ChaincodeException(errorMensaje, WalletManagerErrors.AMOUNTFORMAT_ERROR.toString());
			}

		} catch (Exception e) {
			throw new ChaincodeException(e);
		}

		// Validar si wallet a crear con Id ya existe
		ChaincodeStub stub = ctx.getStub();
		String walletState = stub.getStringState(walletId);
		if (!walletState.isEmpty()) { // Wallet no esta vacio o existe
			String errorMensaje = String.format("El Wallet con Id:[%s ] ya existe", walletId);
			throw new ChaincodeException(errorMensaje, WalletManagerErrors.WALLET_ALREADY_EXISTS.toString());
		}

		// Creando nuevo Wallet con el monto inicial
		Wallet wallet = new Wallet(tokenAmountInicial, owner);
		walletState = genson.serialize(wallet);
		stub.putStringState(walletId, walletState);
		return wallet;

	}

	/**
	 * 
	 * @param ctx
	 * @param walletId
	 * @return
	 */
	@Transaction
	public Wallet getWallet(final Context ctx, final String walletId) {

		ChaincodeStub stub = ctx.getStub();
		String walletState = stub.getStringState(walletId);
		if (walletState.isEmpty()) { // Wallet no esta vacio o existe
			String errorMensaje = String.format("El Wallet con Id:[%s ] no existe", walletId);
			throw new ChaincodeException(errorMensaje, WalletManagerErrors.WALLET_NOT_FOUND.toString());
		}

		Wallet wallet = genson.deserialize(walletState, Wallet.class);
		return wallet;
	}

	@Transaction
	public String transfer2Wallet(	final Context ctx, final String fromWalletId, 
									final String toWalletId, final String tokenAmountStr) {

		ChaincodeStub stub = ctx.getStub();

		// Obtener wallet de origen
		String fromWalletState = stub.getStringState(fromWalletId);
		if (fromWalletState.isEmpty()) { // Wallet no esta vacio o existe
			String errorMensaje = String.format("El Wallet de origen con Id:[%s ] no existe", fromWalletId);
			throw new ChaincodeException(errorMensaje, WalletManagerErrors.WALLET_NOT_FOUND.toString());
		}
		Wallet fromWallet = genson.deserialize(fromWalletState, Wallet.class);

		// Obtener wallet de destino
		String toWalletState = stub.getStringState(toWalletId);
		if (toWalletState.isEmpty()) { // Wallet no esta vacio o existe
			String errorMensaje = String.format("El Wallet de origien con Id:[%s ] no existe", toWalletId);
			throw new ChaincodeException(errorMensaje, WalletManagerErrors.WALLET_NOT_FOUND.toString());
		}
		Wallet toWallet = genson.deserialize(toWalletState, Wallet.class);
		
		//Validar el formato del monto
		Double tokenAmount = 0.0;
		try {
			tokenAmount = Double.valueOf(tokenAmountStr);
			if (tokenAmount < 0.0) {
				String errorMensaje = String.format("Monto a transferir:[%s ] no tiene formato numerico o decimal", tokenAmountStr);
				throw new ChaincodeException(errorMensaje, WalletManagerErrors.AMOUNTFORMAT_ERROR.toString());
			}
			
		}catch (Exception e) {
			throw new ChaincodeException(e);
		}
		
		/*
		 * Validar que existe suficiente fondo monto en Wallet de Origen
		 */
		
		if ( fromWallet.getTokenAmout() < tokenAmount ) {
			String errorMensaje = String.format("El Wallet de origen con Id:[%s ] no tiene suficientes fondos  ", fromWallet.getWalletId());
			throw new ChaincodeException(errorMensaje, WalletManagerErrors.WALLET_NOT_FOUND.toString());	
		}
		
		 
		/**
		 * Realiza la transferencia del monto entre Wallets y grabar el nuevo estado en el libro mayor
		 */
		Wallet newFromWallet = new Wallet(fromWallet.getTokenAmout() - tokenAmount, fromWallet.getWalletId());
		Wallet newToWallet = new Wallet(toWallet.getTokenAmout() + tokenAmount, toWallet.getWalletId());
		
		String newFromWalletState = genson.serialize(newFromWallet);
		String newToWalletState = genson.serialize(newToWallet);
		
		stub.putStringState(fromWallet.getWalletId(), newFromWalletState);
		stub.putStringState(toWallet.getWalletId(), newToWalletState);
		
			

		return "Transferido";
	}

}

