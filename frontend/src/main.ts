import { createApp } from 'vue';
import {
  ActionSheet,
  Button,
  Cell,
  CellGroup,
  DropdownItem,
  DropdownMenu,
  Empty,
  Field,
  Grid,
  GridItem,
  Icon,
  Image,
  NoticeBar,
  Popup,
  PullRefresh,
  Skeleton,
  Tabbar,
  TabbarItem,
  Tag,
  Picker,
  DatePicker,
} from 'vant';
import 'vant/lib/index.css';
import App from './App.vue';
import './styles.css';

const app = createApp(App);
[
  ActionSheet,
  Button,
  Cell,
  CellGroup,
  DropdownItem,
  DropdownMenu,
  Empty,
  Field,
  Grid,
  GridItem,
  Icon,
  Image,
  NoticeBar,
  Popup,
  PullRefresh,
  Skeleton,
  Tabbar,
  TabbarItem,
  Tag,
  Picker,
  DatePicker,
].forEach((comp) => app.use(comp));
app.mount('#app');
