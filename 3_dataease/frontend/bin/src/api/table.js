import Mock from 'mockjs'
const data = Mock.mock({
  'items|30': [{
    id: '@id',
    title: '@sentence(10, 20)',
    'status|1': ['published', 'draft', 'deleted'],
    author: 'name',
    display_time: '@datetime',
    pageviews: '@integer(300, 5000)'
  }]
})
export function getList(params) {
//   return request({
//     url: '/vue-admin-template/table/list',
//     method: 'get',
//     params
//   })
  return new Promise((resolve, reject) => {
    const items = data.items
    const result = {
      code: 20000,
      data: {
        total: items.length,
        items: items
      }
    }
    resolve(result)
  })
}
