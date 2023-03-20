
package es.ucm.fdi.iw.model;

import java.util.ArrayList;
import java.util.List;

/*
 * Calculates debts given the members of a group
 */
public class DebtCalculator {

    public class Tuple {
        public long debtorId;
        public long debtOwnerId;
        public float amount;
    }

    private List<Member> members;

    public DebtCalculator(List<Member> members) {
        this.members = members;
    }

    public List<Tuple> calculateDebts() {
        // TODO
        return new ArrayList<Tuple>();
    }

}

