package haris.hamid.ibkrfacade.data;

import haris.hamid.ibkrfacade.data.holder.ContractHolder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends CrudRepository<ContractHolder, Integer> {

    ContractHolder findContractHolderByOptionChainRequestId(Integer optionChainRequestId);

}
