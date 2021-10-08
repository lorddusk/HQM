package hardcorequesting.common.quests.reward;

import java.util.*;

public class QuestRewardList<T> implements List<QuestReward<T>> {
    
    protected final List<QuestReward<T>> list;
    
    public QuestRewardList() {
        this.list = new LinkedList<>();
    }
    
    public T getReward(int index) {
        QuestReward<T> reward = get(index);
        return reward == null ? null : reward.getReward();
    }
    
    public void setReward(int index, T reward) {
        QuestReward<T> qReward = get(index);
        if (qReward != null)
            qReward.setReward(reward);
    }
    
    //region List implementation
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }
    
    @Override
    public Iterator<QuestReward<T>> iterator() {
        return list.iterator();
    }
    
    @Override
    public Object[] toArray() {
        return list.toArray();
    }
    
    @Override
    public <T1> T1[] toArray(T1[] a) {
        return list.toArray(a);
    }
    
    @Override
    public boolean add(QuestReward<T> t) {
        return list.add(t);
    }
    
    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }
    
    @Override
    public boolean addAll(Collection<? extends QuestReward<T>> c) {
        return list.addAll(c);
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends QuestReward<T>> c) {
        return list.addAll(index, c);
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }
    
    @Override
    public void clear() {
        list.clear();
    }
    
    @Override
    public QuestReward<T> get(int index) {
        return list.get(index);
    }
    
    @Override
    public QuestReward<T> set(int index, QuestReward<T> element) {
        return list.set(index, element);
    }
    
    @Override
    public void add(int index, QuestReward<T> element) {
        list.add(index, element);
    }
    
    @Override
    public QuestReward<T> remove(int index) {
        return list.remove(index);
    }
    
    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }
    
    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }
    
    @Override
    public ListIterator<QuestReward<T>> listIterator() {
        return list.listIterator();
    }
    
    @Override
    public ListIterator<QuestReward<T>> listIterator(int index) {
        return list.listIterator(index);
    }
    
    @Override
    public List<QuestReward<T>> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }
    //endregion
}
