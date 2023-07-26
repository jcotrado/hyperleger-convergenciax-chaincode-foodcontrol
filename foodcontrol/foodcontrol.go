package main

import (
	"encoding/json"
	"fmt"
	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

type SmartContract struct {
	contractapi.Contract
}

// Food provee funciones basicas para el control de comida
type Food struct {
	Agricultor string `json:"agricultor"`
	Variedad string	  `json:"variedad"`
}

// Se definie que esta funcion ahora es parte del smartcontract
func (s *SmartContract) Set(ctx contractapi.TransactionContextInterface, foodId string, agricultor string,  variedad string)  error {

	//Validaciones de sintaxis.

	//Validaciones de Negocios



	//food, err := s.Query(ctx,foodId)
	//if food != nil {
	//		fmt.Printf("Set-> Error - foodid: %s ya existe", err.Error())
	//		return err
	//}

	//Otras Validaciones
	//joel.cotrado++ convierte la estructura en bytes para almacenarla en la DLT
	food := Food{
		Agricultor:  agricultor,
		Variedad: variedad,
	}


	food2Bytes, err := json.Marshal(food)

	if err != nil {										
		fmt.Printf("Marshall error : %s", err.Error())
		//return nil, err
		return err
	}

  // con el stub se puede acceder al ledger distribuido
 return ctx.GetStub().PutState(foodId, food2Bytes)  //Crea el timestamp de la operación y 
}


func (s *SmartContract) Query(ctx contractapi.TransactionContextInterface, foodId string) (*Food, error){

   	food2Bytes, err :=	ctx.GetStub().GetState(foodId)  // sE asigna el estado a variable food2bytes
	if err != nil {
	 	return nil, fmt.Errorf("Falla en la lecutra del estado mundo %s", err.Error())
	}

	if food2Bytes == nil {
		return nil, fmt.Errorf("Identificador :[%s] no existe en la blockchain", foodId )
	}
	food := new(Food)

	err = json.Unmarshal(food2Bytes, food)
	if err != nil {
		return nil, fmt.Errorf ("Query-> Error Unmarshal. %s", err.Error())
    }

	return food, nil
}

func main()  {
	
	// inicializa un chaincode
	chaincode, err := contractapi.NewChaincode(new(SmartContract) )

	if err != nil {
		fmt.Printf("Error creando foodcontrol chaincode %s", err.Error())
		return
	}

	if err := chaincode.Start();  err != nil {
		fmt.Printf("Error iniciando foodcontrol chaincode %s", err.Error())
	}
}