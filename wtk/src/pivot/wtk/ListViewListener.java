/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import pivot.collections.List;

public interface ListViewListener {
    public void listDataChanged(ListView listView, List<?> previousListData);
    public void itemRendererChanged(ListView listView, ListView.ItemRenderer previousItemRenderer);
    public void selectModeChanged(ListView listView, ListView.SelectMode previousSelectMode);
    public void selectedValueKeyChanged(ListView listView, String previousSelectedIndexKey);
    public void selectedValuesKeyChanged(ListView listView, String previousSelectedValuesKey);
}
