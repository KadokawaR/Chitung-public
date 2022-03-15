package lielietea.thevaultnorthkinchovsk.vault;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RegularVault<U,V extends Valuable> implements Vault<U,V> {
    private final Type type;
    private VaultCore<U,V> core;
    private final Lock lock = new ReentrantLock(true);
    private final File serializePath;
    private final BalanceDisplayFormat format;
    private final Gson gson;

    public RegularVault(File serializePath, BalanceDisplayFormat format, VaultCodecComponent<U,V> vaultCodecComponent) {
        this.serializePath = serializePath;
        this.format = format;
        type = new TypeToken<VaultCore<U,V>>() {}.getType();
        gson = new GsonBuilder()
                .registerTypeAdapter(type, new VaultCodec<>(vaultCodecComponent))
                .setPrettyPrinting().create();
        load();
    }

    @Override
    public boolean deposit(U user, V valuable, double amount) {
        return deposit(user, valuable, new BigDecimal(amount));
    }

    @Override
    public boolean deposit(U user, V valuable, BigDecimal amount) {
        if(!(amount.compareTo(BigDecimal.ZERO) >0))
            throw new IllegalArgumentException("deposit amount is invalid");
        else{
            BigDecimal newBalance = core.getBalance(user, valuable).add(amount);
            core.set(user, valuable ,newBalance);
            setChanged();
            return true;
        }
    }

    @Override
    public boolean withdraw(U user, V valuable, double amount) {
        return withdraw(user, valuable, new BigDecimal(amount));
    }

    @Override
    public boolean withdraw(U user, V valuable, BigDecimal amount) {
        if(!(amount.compareTo(BigDecimal.ZERO) >0))
            throw new IllegalArgumentException("withdraw amount is invalid");
        else{
            BigDecimal balance = core.getBalance(user, valuable);
            if(balance.compareTo(amount)<0){
                return false;
            }else{
                BigDecimal newBalance = core.getBalance(user, valuable).subtract(amount);
                if(newBalance.compareTo(BigDecimal.ZERO)==0)
                    core.clearBalance(user, valuable);
                else
                    core.set(user, valuable,newBalance);
                setChanged();
                return true;
            }
        }
    }

    @Override
    public void set(U user, V valuable, double amount) {
        set(user, valuable, new BigDecimal(amount));
    }

    @Override
    public void set(U user, V valuable, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO)==0)
            core.clearBalance(user,valuable);
        else
            core.set(user, valuable, amount);
        setChanged();
    }

    @Override
    public BigDecimal get(U user, V valuable) {
        return core.getBalance(user, valuable);
    }

    @Override
    public String balance(U user, V valuable) {
        return balancePrecise(user,valuable,format);
    }

    @Override
    public String balancePrecise(U user, V valuable, BalanceDisplayFormat format) {
        return core.getDisplayBalance(user, valuable,format);
    }

    @Override
    public boolean clearValuableOf(U user, V valuable) {
        boolean flag = core.clearBalance(user,valuable);
        if(flag)
            setChanged();
        return flag;
    }

    @Override
    public boolean clearAllValuableOf(U user) {
        boolean flag = false;
        for(V valuable :core.getCurrentValuables()){
            if(core.clearBalance(user,valuable))
                flag = true;
        }
        if(flag)
            setChanged();
        return flag;
    }

    @Override
    public void clearAllValuable() {
        core.clearVaultSpaceOnly();
        setChanged();
    }

    @Override
    public Set<V> getCurrentValuables() {
        return core.getCurrentValuables();
    }

    @Override
    public Set<V> getHistoricalValuables() {
        return core.getHistoricalValuables();
    }

    @Override
    public Set<U> getCurrentUsers() {
        return core.getCurrentUsers();
    }

    @Override
    public Set<U> getHistoricalUsers() {
        return core.getHistoricalUsers();
    }

    @Override
    public Optional<Map<U, BigDecimal>> removeValuable(V valuable) {
        Map<U,BigDecimal> m = core.getAllBalanceOfValuable(valuable);
        if(m.isEmpty())
            return Optional.empty();
        core.clearValuableBalance(valuable);
        setChanged();
        return Optional.of(m);
    }

    @Override
    public Optional<Map<V, BigDecimal>> removeUser(U user) {
        Map<V,BigDecimal> m = core.getAllBalanceOfUser(user);
        if(m.isEmpty())
            return Optional.empty();
        core.clearUserBalance(user);
        setChanged();
        return Optional.of(m);
    }

    @Override
    public Optional<Map<U, BigDecimal>> getUserOf(V valuable) {
        Map<U,BigDecimal> m = core.getAllBalanceOfValuable(valuable);
        if(m.isEmpty())
            return Optional.empty();
        else
            return Optional.of(m);
    }

    @Override
    public Optional<Map<V, BigDecimal>> getValuablesOf(U user) {
        Map<V,BigDecimal> m = core.getAllBalanceOfUser(user);
        if(m.isEmpty())
            return Optional.empty();
        else
            return Optional.of(m);
    }

    @Override
    public void reloadFromJson() {
        load();
    }

    private void setChanged() {
        save();
    }

    private void load(){
        lock.lock();
        try {
            if(!serializePath.exists()){
                try(BufferedWriter out = new BufferedWriter(new FileWriter(serializePath))){
                    out.write("{}");
                    serializePath.createNewFile();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
            try(BufferedReader in = new BufferedReader(new FileReader(serializePath))) {
                core = gson.fromJson(in, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            lock.unlock();
        }
    }

    private void save(){
        lock.lock();
        try {
            try(BufferedWriter out = new BufferedWriter(new FileWriter(serializePath))) {
                out.write(gson.toJson(core, type));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            lock.unlock();
        }
    }

}
