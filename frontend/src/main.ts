import { createApp } from 'vue';
import {
  Button,
  Cell,
  CellGroup,
  DropdownItem,
  DropdownMenu,
  Empty,
  Field,
  Form,
  Icon,
  Image,
  Popup,
  Skeleton,
  Tabbar,
  TabbarItem,
  Tag,
  Toast,
} from 'vant';
import 'vant/lib/index.css';
import App from './App.vue';
import './styles.css';

const app = createApp(App);
[
  Button,
  Cell,
  CellGroup,
  DropdownItem,
  DropdownMenu,
  Empty,
  Field,
  Form,
  Icon,
  Image,
  Popup,
  Skeleton,
  Tabbar,
  TabbarItem,
  Tag,
  Toast,
].forEach((c) => app.use(c));
app.mount('#app');
