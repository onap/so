package org.openecomp.mso.client.aai.entities.bulkprocess;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"transactions"
})
public class Transactions {

@JsonProperty("transactions")
private List<Transaction> transactions = new ArrayList<>();

@JsonProperty("transactions")
public List<Transaction> getTransactions() {
return transactions;
}

@JsonProperty("transactions")
public void setTransactions(List<Transaction> transactions) {
this.transactions = transactions;
}

public Transactions withTransactions(List<Transaction> transactions) {
this.transactions = transactions;
return this;
}

}