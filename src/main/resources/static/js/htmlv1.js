

/**
 * @param {any} strs
 * @param {any[]} vals
 */
export function html(strs,...vals){
	return String.raw({raw:strs},vals);
}