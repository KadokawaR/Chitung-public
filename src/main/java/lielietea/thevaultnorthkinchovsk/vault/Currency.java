package lielietea.thevaultnorthkinchovsk.vault;

public abstract class Currency implements Valuable {

    public static Currency of(String tag){
        return new Currency() {
            @Override
            public String tag() {
                return tag;
            }
        };
    }

    @Override
    public int hashCode() {
        return tag().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Currency)
            return ((Currency) obj).tag().equals(tag());
        return false;
    }
}
