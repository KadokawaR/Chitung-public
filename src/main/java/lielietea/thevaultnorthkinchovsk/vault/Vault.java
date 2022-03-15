package lielietea.thevaultnorthkinchovsk.vault;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Vault<U,V extends Valuable> {
    boolean deposit(U user, V valuable, double amount);

    boolean deposit(U user, V valuable, BigDecimal amount);

    boolean withdraw(U user, V valuable, double amount);

    boolean withdraw(U user, V valuable, BigDecimal amount);

    void set(U user, V valuable, double amount);

    void set(U user, V valuable, BigDecimal amount);

    BigDecimal get(U user, V valuable);

    String balance(U user, V valuable);

    String balancePrecise(U user, V valuable, BalanceDisplayFormat format);

    boolean clearValuableOf(U user, V valuable);

    boolean clearAllValuableOf(U user);

    void clearAllValuable();

    Set<V> getCurrentValuables();

    Set<V> getHistoricalValuables();

    Set<U> getCurrentUsers();

    Set<U> getHistoricalUsers();

    Optional<Map<U,BigDecimal>> removeValuable(V valuable);

    Optional<Map<V,BigDecimal>> removeUser(U user);

    Optional<Map<U,BigDecimal>> getUserOf(V valuable);

    Optional<Map<V,BigDecimal>> getValuablesOf(U user);

    void reloadFromJson();
}
