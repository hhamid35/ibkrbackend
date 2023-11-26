package haris.hamid.ibkrbackend.data;

import haris.hamid.ibkrbackend.data.holder.ContractHolder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends CrudRepository<ContractHolder, Integer> {

    ContractHolder findContractHolderByOptionChainRequestId(Integer optionChainRequestId);

}
