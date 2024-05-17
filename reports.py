import pandas as pd
import os

# get filenames in a folder
def get_csv_filenames(folder):
    filenames = []
    for filename in os.listdir(folder):
        if filename.endswith('.csv'):
            filenames.append(filename)
    return filenames


def remove_outliers(df):
    # Calculate Q1 (25th percentile) and Q3 (75th percentile)
    Q1 = df['ResponseTime'].quantile(0.25)
    Q3 = df['ResponseTime'].quantile(0.75)
    IQR = Q3 - Q1

    # Define the lower and upper bounds for outliers
    lower_bound = Q1 - 1.5 * IQR
    upper_bound = Q3 + 1.5 * IQR

    # Filter the DataFrame to remove outliers
    df_no_outliers = df[(df['ResponseTime'] >= lower_bound) & (df['ResponseTime'] <= upper_bound)]
    return df_no_outliers

# read one csv file into df
def read_csv_file(folder, filename):
    path = os.path.join(folder, filename)
    df = pd.read_csv(path)
    # df = df[10:]
    return df


# read all csv files in a folder
def read_csv_files(folder):
    filenames = get_csv_filenames(folder)
    # get length of filenames
    len = filenames.__len__()
    dfs = []
    for filename in filenames:
        df = read_csv_file(folder, filename)
        df['nodes']=len
        df = remove_outliers(df)
        dfs.append(df)
    return dfs

# for each df, calculate max value of index column, add one
# call this variable k
# group each consecutive k rows into one row
# for variable startTime, take min value in the k rows
# for variable endTime, take max value in the k rows
# for variable Response Time, take average
# ignore Batch,BatchOutput,Index columns
# sum addOps, deleteOps, queryOps, batchSize, timeSleptAfterThisBatch
# add another meanBatchSize column, which is batchSize/k
# add another meanTimeSleptAfterThisBatch column, which is timeSleptAfterThisBatch/k
# return a list of dfs that contains each row as summary of the k rows described above
def summarize(dfs):
    summarized_dfs = []
    
    for df in dfs:
        max_index = df['Index'].max()
        k = max_index + 1
        # add run column to each k group
#Index,StartTimestamp,EndTimestamp,ResponseTime,Batch,BatchOutput,writePercentage,addOps,deleteOps,queryOps,batchSize,timeSleptAfterThisBatch

        df['run'] = (df.index) // k
        # print("k is ", k, " df index is ", df.index)
        # print(df['run'])
        # print(df.head(10))
        summarized_df = df.groupby('run').agg({
            'StartTimestamp': 'min',
            'EndTimestamp': 'max',
            'ResponseTime': 'mean',
            'addOps': 'sum',
            'deleteOps': 'sum',
            'queryOps': 'sum',
            'batchSize': 'sum',
            'timeSleptAfterThisBatch': 'sum',
            'writePercentage':'mean',
            'nodes':'mean'
        })
        summarized_df['numBatches'] = k
        summarized_df['meanBatchSize'] = summarized_df['batchSize'] / k
        summarized_df['meanTimeSleptAfterThisBatch'] = summarized_df['timeSleptAfterThisBatch'] / k
        summarized_dfs.append(summarized_df)
    return summarized_dfs



# merging summarized dfs into one
# each entry has same run number, so we can merge on run number
# make start time to be min of all start times
# make end time to be max of all end times
# make Response Time to be average of all Response Times
# make addOps to be sum of all addOps
# make deleteOps to be sum of all deleteOps
# make queryOps to be sum of all queryOps
# make batchSize to be sum of all batchSize
# make timeSleptAfterThisBatch to be sum of all timeSleptAfterThisBatch
# make meanBatchSize to be average of all meanBatchSize
# make meanTimeSleptAfterThisBatch to be average of all meanTimeSleptAfterThisBatch
# return a single df
def merge_summarized_dfs(summarized_dfs):
    df = pd.concat(summarized_dfs)
    df = df.groupby('run').agg({
        'StartTimestamp': 'min',
        'EndTimestamp': 'max',
        'numBatches': 'sum',
        'ResponseTime': 'mean',
        'addOps': 'sum',
        'deleteOps': 'sum',
        'queryOps': 'sum',
        'batchSize': 'sum',
        'timeSleptAfterThisBatch': 'sum',
        'meanBatchSize': 'mean',
        'meanTimeSleptAfterThisBatch': 'mean',
        'writePercentage':'mean',
        'nodes':'mean'
    })
    return df


# for each run calculate frequency which is number of batches / total time
# return table (df) with run number and frequency vs response time
def calculate_frequency_vs_response_time(df):
    df['frequency'] = 1000*df['numBatches'] /                          \
                ((df['EndTimestamp'] - df['StartTimestamp']))
    return df[['frequency', 'ResponseTime']]


# for each run calculate response time vs write percentage
# return table (df) with run number and write percentage vs response time
def calculate_write_percentage_vs_response_time(df):
    df['calculatedWritePercentage'] = (df['addOps']+ df['deleteOps']) / (df['addOps']+ df['deleteOps'] + df['queryOps'])*100
    return df[['calculatedWritePercentage', 'ResponseTime']]


# calculate response time vs number of nodes
def calculate_nodes_vs_response_time(df):
    return df[['nodes', 'ResponseTime']]



# get all folder names in folder "experiments"
def get_folder_names(folder):
    folder_names = []
    for filename in os.listdir(folder):
        if os.path.isdir(os.path.join(folder, filename)):
            folder_names.append(filename)
    return folder_names

experiments_folders = get_folder_names('experiments')

wp_vs_rt_table = pd.DataFrame()
f_vs_rt_table = pd.DataFrame()
c_vs_rt_table = pd.DataFrame()

for folder in experiments_folders:
    folder = os.path.join('experiments', folder)
    print("Processing folder ", folder)
    dfs = read_csv_files(folder)


    summarized_dfs = summarize(dfs)
    merged_df = merge_summarized_dfs(summarized_dfs)
    # if folders contains write
    if(folder.find("write") != -1):
        #append to write_percentage_vs_response_time_table
        wp_vs_rt_table = wp_vs_rt_table._append(calculate_write_percentage_vs_response_time(merged_df))
    # if folders contains clients
    if(folder.find("sleep") != -1):
        #append to frequency_vs_response_time_table
        f_vs_rt_table = f_vs_rt_table._append(calculate_frequency_vs_response_time(merged_df))
    
    if(folder.find("clients") != -1):
        #append to nodes_vs_response_time_table
        c_vs_rt_table = c_vs_rt_table._append(calculate_nodes_vs_response_time(merged_df))


#sort the dataframe by the first column
wp_vs_rt_table = wp_vs_rt_table.sort_values(by='calculatedWritePercentage')
f_vs_rt_table = f_vs_rt_table.sort_values(by='frequency')
c_vs_rt_table = c_vs_rt_table.sort_values(by='nodes')


# create results directory if it doesn't exist
if not os.path.exists('results'):
    os.makedirs('results')

# save each table to csv, overwrite
wp_vs_rt_table.to_csv('results/write_percentage_vs_response_time.csv', index=False)
f_vs_rt_table.to_csv('results/frequency_vs_response_time.csv', index=False)
c_vs_rt_table.to_csv('results/nodes_vs_response_time.csv', index=False)





experiments_folders = get_folder_names('experiments_inc')

wp_vs_rt_table = pd.DataFrame()
f_vs_rt_table = pd.DataFrame()
c_vs_rt_table = pd.DataFrame()

for folder in experiments_folders:
    folder = os.path.join('experiments_inc', folder)
    print("Processing folder ", folder)
    dfs = read_csv_files(folder)
    summarized_dfs = summarize(dfs)
    merged_df = merge_summarized_dfs(summarized_dfs)
    # if folders contains write
    if(folder.find("write") != -1):
        #append to write_percentage_vs_response_time_table
        wp_vs_rt_table = wp_vs_rt_table._append(calculate_write_percentage_vs_response_time(merged_df))
    # if folders contains clients
    if(folder.find("sleep") != -1):
        #append to frequency_vs_response_time_table
        f_vs_rt_table = f_vs_rt_table._append(calculate_frequency_vs_response_time(merged_df))
    
    if(folder.find("clients") != -1):
        #append to nodes_vs_response_time_table
        c_vs_rt_table = c_vs_rt_table._append(calculate_nodes_vs_response_time(merged_df))

#sort the dataframe by the first column
# wp_vs_rt_table = wp_vs_rt_table.sort_values(by='calculatedWritePercentage')
f_vs_rt_table = f_vs_rt_table.sort_values(by='frequency')
c_vs_rt_table = c_vs_rt_table.sort_values(by='nodes')

# create results directory if it doesn't exist
if not os.path.exists('results_inc'):
    os.makedirs('results_inc')

# save each table to csv, overwrite
# wp_vs_rt_table.to_csv('results_inc/write_percentage_vs_response_time_inc.csv', index=False)
f_vs_rt_table.to_csv('results_inc/frequency_vs_response_time_inc.csv', index=False)
c_vs_rt_table.to_csv('results_inc/nodes_vs_response_time_inc.csv', index=False)
