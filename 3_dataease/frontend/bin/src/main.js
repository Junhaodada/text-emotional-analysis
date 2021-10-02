import Vue from 'vue'
import Cookies from 'js-cookie'
import '@/styles/index.scss' // global css
import ElementUI from 'element-ui'
import Fit2CloudUI from 'fit2cloud-ui'
// import axios from 'axios'
// import VueAxios from 'vue-axios'
import i18n from './lang' // internationalization
import App from './App'
import store from './store'
import router from './router'
import message from './utils/message'
import '@/icons' // icon
import '@/permission' // permission control
import api from '@/api/index.js'
import filter from '@/filter/filter'
import directives from './directive'
import VueClipboard from 'vue-clipboard2'
import widgets from '@/components/widget'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import './utils/dialog'
import DeComplexInput from '@/components/business/condition-table/DeComplexInput'
import DeComplexSelect from '@/components/business/condition-table/DeComplexSelect'
import '@/components/canvas/custom-component' // 注册自定义组件
Vue.config.productionTip = false
Vue.use(VueClipboard)
Vue.use(widgets)
Vue.prototype.$api = api

import * as echarts from 'echarts'

Vue.prototype.$echarts = echarts

import UmyUi from 'umy-ui'
Vue.use(UmyUi)

import vcolorpicker from 'vcolorpicker'
Vue.use(vcolorpicker)

// 全屏插件
import fullscreen from 'vue-fullscreen'
Vue.use(fullscreen)

// import TEditor from '@/components/Tinymce/index.vue'
// Vue.component('TEditor', TEditor)

/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online ! ! !
 */
if (process.env.NODE_ENV === 'production') {
//   const { mockXHR } = require('../mock')
//   mockXHR()
}

// set ElementUI lang to EN
// Vue.use(ElementUI, { locale })
// 如果想要中文版 element-ui，按如下方式声明
Vue.use(ElementUI, {
  size: Cookies.get('size') || 'medium', // set element-ui default size
  i18n: (key, value) => i18n.t(key, value)
})
Vue.use(Fit2CloudUI, {
  i18n: (key, value) => i18n.t(key, value)
})
// Vue.use(VueAxios, axios)
Vue.use(filter)
Vue.use(directives)
Vue.use(message)
Vue.component('Treeselect', Treeselect)
Vue.component('DeComplexInput', DeComplexInput)
Vue.component('DeComplexSelect', DeComplexSelect)
Vue.config.productionTip = false

import vueToPdf from 'vue-to-pdf'
Vue.use(vueToPdf)

Vue.prototype.hasDataPermission = function(pTarget, pSource) {
  if (this.$store.state.user.user.isAdmin) {
    return true
  }
  if (pSource && pTarget) {
    return pSource.indexOf(pTarget) > -1
  }
  return false
}

Vue.prototype.checkPermission = function(pers) {
  const permissions = store.getters.permissions
  const hasPermission = pers.every(needP => {
    const result = permissions.includes(needP)
    return result
  })
  return hasPermission
}
new Vue({

  router,
  store,
  i18n,
  render: h => h(App)
}).$mount('#app')
