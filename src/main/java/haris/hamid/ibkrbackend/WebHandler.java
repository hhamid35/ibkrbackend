package haris.hamid.ibkrbackend;

import com.ib.client.Contract;
import com.ib.client.Types;
import haris.hamid.ibkrbackend.data.holder.*;
import haris.hamid.ibkrbackend.service.ContractManagerService;
import haris.hamid.ibkrbackend.service.OrderManagerService;
import haris.hamid.ibkrbackend.service.PositionManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web endpoints for accessing TWS functionality
 */
@Slf4j
@RestController
@DependsOn("TWS")
public class WebHandler {

    private ContractManagerService contractManagerService;
    private OrderManagerService orderManagerService;
    private PositionManagerService positionManagerService;

    /**
     * This variable decides if "high frequency" data stream is needed from IBKR.
     * It's value can be set from application.properties file.
     */
    @Value("${ibkr.tick-by-tick-stream}")
    private boolean tickByTickStream;

    // @Autowired
    WebHandler(ContractManagerService contractManagerService, OrderManagerService orderManagerService,
            PositionManagerService positionManagerService) {
        this.contractManagerService = contractManagerService;
        this.orderManagerService = orderManagerService;
        this.positionManagerService = positionManagerService;
    }

    @Operation(summary = "Search for an instrument by it's ticker, or part of it's name.", parameters = {
            @Parameter(description = "Ticker, or name of the traded instrument", examples = {
                    @ExampleObject(name = "Ticker", value = "AAPL"),
                    @ExampleObject(name = "Company name", value = "Apple")
            })
    })

    @GetMapping("/search")
    List<ContractHolder> searchContract(@RequestParam String query) {
        return contractManagerService.searchContract(query).stream()
                .map(ContractHolder::new)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Subscribes to an instrument by it's conid. Subscription means the TWS starts streaming the market data for the instrument which will be saved into Redis TimeSeries", parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument")
    })

    @GetMapping("/subscribe/{conid}")
    void subscribeMarketDataByConid(@PathVariable int conid) {
        contractManagerService.subscribeMarketData(getContractByConid(conid).getContract(), tickByTickStream);
    }

    @Operation(summary = "Returns with the ContractHolder which contains the Contract descriptor itself and the streamRequestId if you are already subscribed to the instrument.", parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument")
    })

    @GetMapping("/contract/{conid}")
    ContractHolder getContractByConid(@PathVariable int conid) {
        return contractManagerService.getContractHolder(conid);
    }

    @Operation(summary = "Sends an order to the market.", parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument"),
            @Parameter(name = "action", description = "BUY or SELL"),
            @Parameter(name = "quantity", description = "Quantity"),
            @Parameter(name = "price", description = "Price value")
    })
    @PostMapping("/order")
    void placeOrder(@RequestParam int conid, @RequestParam String action, @RequestParam double quantity,
            @RequestParam double price) {
        Contract contract = contractManagerService.getContractHolder(conid).getContract();
        orderManagerService.placeLimitOrder(contract, Types.Action.valueOf(action), quantity, price);
    }

    @Operation(summary = "Returns with the list of orders.")
    @GetMapping("/orders")
    Collection<OrderHolder> getAllOrders() {
        return orderManagerService.getAllOrders();
    }

    @Operation(summary = "Returns with the list of active orders.")
    @GetMapping("/orders/active")
    Collection<OrderHolder> getActiveOrders() {
        return orderManagerService.getActiveOrders();
    }

    @Operation(summary = "Returns with the list of open positions.")
    @GetMapping("/positions")
    Collection<PositionHolder> getAllPositions() {
        return positionManagerService.getAllPositions();
    }

    @Operation(summary = "Returns with the last available bid and ask for the given Contract.", parameters = {
            @Parameter(name = "contract", description = "The Contract descriptor object as a JSON")
    })
    @PostMapping("/price")
    PriceHolder getLastPriceByContract(@RequestBody Contract contract) {
        return contractManagerService.getLastPriceByContract(contract);
    }

    @Operation(summary = "Returns with the last available bid and ask by conid.", parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument")
    })
    @GetMapping("/price/{conid}")
    PriceHolder getLastPriceByConid(@PathVariable int conid) {
        return contractManagerService.getLastPriceByConid(conid);
    }

    @Operation(summary = "Returns with the option chain as the list of option typed Contracts for an underlying Contract. " +
            "This method won't subscribe to the changes of the certain options. If you need the option data as a market stream, " +
            "you have to subscribe to each option you want one-by-one.", parameters = {@Parameter(name = "underlyingConid", description = "The conid (IBKR unique id) of the instrument") })
    @GetMapping("/optionChain/{underlyingConid}")
    Collection<Option> getOptionChain(@PathVariable int underlyingConid) {
        return contractManagerService.getOptionChainByConid(underlyingConid);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<String> handleAllUncaughtException(Exception exception, WebRequest request) {
        log.error("Uncaught Error: " + exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }
}
