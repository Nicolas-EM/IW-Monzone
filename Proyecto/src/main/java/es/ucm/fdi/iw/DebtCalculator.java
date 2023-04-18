
package es.ucm.fdi.iw;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import es.ucm.fdi.iw.model.Debt;
import es.ucm.fdi.iw.model.DebtID;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.User;
import lombok.AllArgsConstructor;

/*
 * Calculates debts given the members of a group
 */
public class DebtCalculator {

    @AllArgsConstructor
    public class Balance implements Comparable<Balance> {
        public User user;
        public float balance;

        @Override
        public int compareTo(Balance other) {
            return Float.compare((Math.abs(other.balance)), Math.abs(this.balance));
        }
    }

    public List<Debt> calculateDebts(List<Member> members, Group group) {

        PriorityQueue<Balance> positiveB = new PriorityQueue<>();
        PriorityQueue<Balance> negativeB = new PriorityQueue<>();

        // create priority queues
        for (Member m : members) {                 
            if (m.getBalance() < 0)
                negativeB.add(new Balance(m.getUser(), m.getBalance()));
            else if (m.getBalance() > 0)
                positiveB.add(new Balance(m.getUser(), m.getBalance()));
        }

        List<Debt> debts = new ArrayList<>();
        while (!positiveB.isEmpty()) {
            // get top of both queues
            Balance pos = positiveB.poll();
            Balance neg = negativeB.poll();

            // create transaction
            float amount = Math.min(Math.abs(pos.balance), Math.abs(neg.balance));
            User debtor = neg.user;
            User debtOwner = pos.user;
            DebtID dId = new DebtID(group.getId(), debtor.getId(), debtOwner.getId());
            debts.add(new Debt(dId, amount, group, debtor, debtOwner));
            // get spare balance and insert on queue
            float balance = pos.balance + neg.balance;
            
            if (Math.abs(balance) >= 0.01 && balance < 0)
                 negativeB.add(new Balance(debtor, balance));
            else if (Math.abs(balance) >= 0.01 && balance > 0)
                positiveB.add(new Balance(debtOwner, balance));
        }
        return debts;
    }

}
