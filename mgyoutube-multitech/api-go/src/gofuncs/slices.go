package gofuncs

// FindStringIndexInStringSlice - returns the index of the value string in the provided string slice, or -1 if not found
func FindStringIndexInStringSlice(s []string, value string) int {
	for i, v := range s {
		if v == value {
			return i
		}
	}
	return -1
}

// RemoveIndexFromSlice - returns a copy of the provided string slice without the item at index
func RemoveIndexFromSlice(s []string, index int) []string {
	returnSlice := make([]string, 0)
	returnSlice = append(returnSlice, s[:index]...)
	return append(returnSlice, s[index+1:]...)
}
