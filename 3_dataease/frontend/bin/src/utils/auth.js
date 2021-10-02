import Cookies from 'js-cookie'
import Config from '@/settings'

const TokenKey = Config.TokenKey

const IdTokenKey = Config.IdTokenKey

const AccessTokenKey = Config.AccessTokenKey

const linkTokenKey = Config.LinkTokenKey

export function getIdToken() {
  return Cookies.get(IdTokenKey)
}

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  Cookies.remove(IdTokenKey)
  Cookies.remove(AccessTokenKey)
  return Cookies.remove(TokenKey)
}

export function getLinkToken() {
  return Cookies.get(linkTokenKey)
}

export function setLinkToken(token) {
  return Cookies.set(linkTokenKey, token)
}

export function removeLinkToken() {
  return Cookies.remove(linkTokenKey)
}

export function setSysUI(uiInfo) {
  return Cookies.set('sysUiInfo', uiInfo ? JSON.stringify(uiInfo) : null)
}

export function getSysUI() {
  const json = Cookies.get('sysUiInfo')
  return json ? JSON.parse(json) : null
}

