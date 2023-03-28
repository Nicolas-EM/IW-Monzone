
package es.ucm.fdi.iw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import ch.qos.logback.core.joran.conditional.ElseAction;
import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * Calculates debts given the members of a group
 */
public class DebtCalculator {

    @Data
    @AllArgsConstructor
    public class Tuple {
        public long debtorId;
        public String debtorName;
        public long debtOwnerId;
        public String debtOwnerName;
        public float amount;
    }

    public class Balance implements Comparable<Balance> {
        public long userId;
        public String username;
        public float balance;

        public Balance(long userId, String username, float balance) {
            this.userId = userId;
            this.username = username;
            this.balance = balance;
        }

        @Override
        public int compareTo(Balance other) {
            return Float.compare((Math.abs(other.balance)), Math.abs(this.balance));
        }
    }

    private PriorityQueue<Balance> positiveB;
    private PriorityQueue<Balance> negativeB;

    public DebtCalculator(List<Member> members) {
        this.positiveB = new PriorityQueue<>();
        this.negativeB = new PriorityQueue<>();

        // create priority queues
        for (Member m : members) {
            if (m.getBalance() < 0)
                negativeB.add(new Balance(m.getUser().getId(), m.getUser().getUsername(), m.getBalance()));
            else if (m.getBalance() > 0)
                positiveB.add(new Balance(m.getUser().getId(), m.getUser().getUsername(), m.getBalance()));
        }
    }

    public List<Tuple> calculateDebts() {
        List<Tuple> debts = new ArrayList<>();
        // both queues will get empty at the same time
        if (positiveB.isEmpty())
            debts.add(new Tuple(-1, "nada", -2, "nada", 0));
        while (!positiveB.isEmpty()) {
            // get top of both queues
            Balance pos = positiveB.poll();
            Balance neg = negativeB.poll();
            // create transaction
            float amount = Math.min(Math.abs(pos.balance), Math.abs(neg.balance));
            long debtorId = neg.userId;
            String debtorName = neg.username;
            long debtOwnerId = pos.userId;
            String debtOwnerName = pos.username;
            debts.add(new Tuple(debtorId, debtorName, debtOwnerId, debtOwnerName, amount));
            // get spare balance and insert on queue
            float balance = pos.balance + neg.balance;
            if (balance < 0)
                 negativeB.add(new Balance(debtorId, debtorName, balance));
            else
                positiveB.add(new Balance(debtOwnerId, debtOwnerName, balance));
        }
        return debts;
    }

}
