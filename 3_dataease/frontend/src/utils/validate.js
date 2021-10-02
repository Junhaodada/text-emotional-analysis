/**
 * Created by PanJiaChen on 16/11/18.
 */

/**
 * @param {string} path
 * @returns {Boolean}
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validUsername(str) {
  const valid_map = ['admin', 'cyw']
  return valid_map.indexOf(str.trim()) >= 0
}

export const PHONE_REGEX = '^1[3|4|5|7|8][0-9]{9}$'
