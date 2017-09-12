import java.util.Set;


abstract class CrossworkProgressListener {
	
	abstract void updateDictionaryProgress(double progress);
	abstract void dictionaryLoadingComplete();
	abstract void updateSortingProgress(double progress);
	abstract void sortingComplete(Set<XWord> sortedList);
	
}
