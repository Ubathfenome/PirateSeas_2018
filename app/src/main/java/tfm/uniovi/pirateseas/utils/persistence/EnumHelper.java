package tfm.uniovi.pirateseas.utils.persistence;

public class EnumHelper<T extends Enum<T>> {

    private T t;

    /**
     * Get the enum's type index of the specified enum
     * @param enumItem Enum item
     * @return Index
     */
    public int getEnumIndex(Enum<T> enumItem){
        int index = 0;
        Enum<T>[] enumValues = enumItem.getDeclaringClass().getEnumConstants();
        for(int i = 0; i < enumValues.length; i++){
            Enum<T> st = enumValues[i];
            if(enumItem.name().equals(st.name()))
                index = i;
        }
        return index;
    }
}
